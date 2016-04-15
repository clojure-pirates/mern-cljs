(ns mern-utils.passport.strategy
  (:require-macros
    [mern-utils.macros :refer [node-require, node-config]]
    [mern-utils.passport.macros :refer [def-strategy-cb]])
  (:require
    [cljs.nodejs :as nodejs]
    [clojure.string :as string]
    [cljs-time.core :as time-core]
    [cljs-time.coerce :as time-coerce]
    [hasch.core :as hasch]
    [mern-utils.db :as db]
    [mern-utils.lib :refer [raise]]))

(node-require passport-local "passport-local")
(def local-strategy (.-Strategy passport-local))

(node-require passport-facebook "passport-facebook")
(def facebook-strategy (.-Strategy passport-facebook))

(node-require passport-google "passport-google-oauth")
(def google-strategy (.-OAuth2Strategy passport-google))

(defn uuid4-hex []
  (string/replace (str (hasch/uuid)) "-" ""))

(defn create-uid [] (uuid4-hex))

(defn create-api-token []
  {:token (uuid4-hex) :created-at (time-coerce/to-long (time-core/now))})

(defn validate-api-token
  "Returns true if token is valid for the user"
  [api-token given-token token-expires-in-sec]
  (let [expire-at (+ (* 1000 token-expires-in-sec) (.-tokenCreatedAt api-token))]
    (if (< expire-at (time-coerce/to-long (time-core/now)))
      false
      (if (and (not-empty (.-token api-token)) (= given-token (.-token api-token)))
        true false))))

(defn get-full-name [profile]
  (if (not= (.. profile -name -givenName) js/undefined)
    (str (.. profile -name -givenName) " " (.. profile -name -familyName))
    (.. profile -name)))

(defn get-email [profile]
  (if (not= (.-emails profile) js/undefined)
    (.-value (first (.-emails profile)))
    nil))

(defn get-photo [profile]
  (if (not= (.-photos profile) js/undefined)
    (.-value (first (.-photos profile)))
    nil))

(defn upsert-record [model query data then]
  (db/upsert model query data
             (fn [err & record]
               (if err (raise err)
                 (then record)))))

(defn create-record [model data then]
  (db/create model data
             (fn [err record] (if err (raise err) (then record)))))

(defn refresh-api-token [api-token-model user-uid then]
  (println "Refreshing API token for " user-uid)
  (let [api-token (create-api-token)
        data {:token (:token api-token)
              :tokenCreatedAt (:created-at api-token)}
        query {:userUid user-uid}]
  (db/upsert api-token-model query data
             (fn [err record] (if err (raise err) (then record))))))

(defn get-user-from-social-account-id [user-model social-account-model id then]
  (db/get-by-id social-account-model id
                (fn [err acct]
                  (if err
                    (then err nil)
                    (if acct
                      (db/get-one user-model {:uid (.-userUid acct)} then)
                      (then nil nil))))))

(defn register-new-user [strategy
                         user-model api-token-model social-account-model
                         email-domain-restriction token profile done]
  (println "Registering a new user")
  (if (and email-domain-restriction
           (or (= (.-emails profile) js/undefined)
               (not= email-domain-restriction
                     (last (string/split (.-value (first (.-emails profile))) #"@")))))
    (done (str "Registration only allowed for " email-domain-restriction) nil)
    (let [full-name   (get-full-name profile)
          email       (get-email profile)
          photo       (get-photo profile)
          user-uid    (create-uid)
          user-data   {:uid user-uid
                       :name full-name
                       :email email
                       :photo photo}
          social-acct {:id (.-id profile)
                       :userUid user-uid
                       :token token
                       :name full-name
                       :email email
                       :photo photo}]
      (create-record
        social-account-model social-acct
        (fn [acct]
          (refresh-api-token
            api-token-model (.-userUid acct)
            (fn [api-token]
              (create-record
                user-model user-data
                (fn [new-user] (done nil new-user))))))))))

(defn config-passport [passport config-auth user-model api-token-model facebook-account-model google-account-model]
  ; =========================================================================
  ; passport session setup ==================================================
  ; =========================================================================
  ; required for persistent login sessions
  ; passport needs ability to serialize and unserialize users out of session

  ; used to serialize the user for the session
  (.serializeUser passport (fn [user done] (done nil (.-uid user))))

  ; used to deserialize the user
  (.deserializeUser
    passport
    (fn [id done]
      (db/get-one user-model {:uid id}
                  (fn [err user]
                    (db/get-one api-token-model {:userUid id}
                                (fn [err api-token]
                                  (aset user "api" api-token)
                                  (done err user)))))))

  ; =========================================================================
  ; API LOGIN =============================================================
  ; =========================================================================
  ; we are using named strategies since we have one for login and one for signup
  ; by default, if there was no name, it would just be called 'local'
  (.use
    passport
    "local-login"
    (local-strategy.
      (clj->js
        {; by default, local strategy uses username and password, we will override with uid and token
         :usernameField "uid"
         :passwordField "token"
         :passReqToCallback true})
      ; callback with uid and token from our form
      (fn [req uid token done]
        ; find a user whose email is the same as the forms email
        ; we are checking to see if the user trying to login already exists
        (db/get-one
          user-model {:uid uid}
          (fn [err user]
            ; if there are any errors, return the error before anything else
            (if (not (nil? err))
              (done err)
              (if (not (nil? user))
                (db/get-one
                  api-token-model {:userUid (.-uid user)}
                  (fn [err api-token]
                    (if (validate-api-token api-token token (:token-expires-in-sec config-auth))
                      ; all is well, return successful user
                      (done nil user)
                      ; create the loginMessage and save it to session as flashdata
                      (done nil false (.flash req "loginMessage" "Oops! Wrong token.")))))
                ; req.flash is the way to set flashdata using connect-flash
                (done nil false  (.flash req "loginMessage" "No user found."))))))))

  (def-strategy-cb "facebook" config-auth user-model api-token-model facebook-account-model)
  (def-strategy-cb "google" config-auth user-model api-token-model google-account-model)
    ))
