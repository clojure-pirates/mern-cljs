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
    [mern-utils.lib :refer [str->hex]]))

(node-require passport-local "passport-local")
(def local-strategy (.-Strategy passport-local))

(node-require passport-facebook "passport-facebook")
(def facebook-strategy (.-Strategy passport-facebook))

(node-require passport-google "passport-google-oauth")
(def google-strategy (.-OAuth2Strategy passport-google))

(defn add-uid [user salt]
  (if (not (.-uid user))
    (set! (.-uid user) (str->hex (str (hasch/uuid salt)))))
  user)

(defn give-api-token
  [user]
  ; This function does NOT commit the change to the database
  (set! (.. user -api -token) (str->hex (str (hasch/uuid)))) ; uuid4
  (set! (.. user -api -tokenExpiresAt) (+ (* 1000 60 60 24 365) (time-coerce/to-long (time-core/now))))
  user)

(defn get-api-token [user]
  (.. user -api -token))

(defn validate-api-token
  "Returns true if token is valid for the user"
  [user token]
  (let [token (.. user -api -token)
        expire-at (.. user -api -tokenExpiresAt)]
    (if (< expire-at (time-coerce/to-long (time-core/now)))
      false
      (if (and (not-empty (.. user -api -token)) (= token (.. user -api -token)))
        true false))))

(defn register-new-user [strategy user-model email-domain-restriction token profile done]
  (if (and email-domain-restriction
           (or (= (.-emails profile) js/undefined)
               (not= email-domain-restriction
                     (last (string/split (.-value (first (.-emails profile))) #"@")))))
    (done (str "Registration only allowed for " email-domain-restriction) nil)
    (let [new-user (new user-model)
          strategy-type (symbol (str "-" strategy))]
      (aset new-user strategy "id" (.-id profile))
      (aset new-user strategy "token" token)

      ; facebook gives givenName+familyName and Google is just flat name
      (if (not= (.. profile -name -givenName) js/undefined)
        (aset new-user strategy "name"
              (str (.. profile -name -givenName) " " (.. profile -name -familyName)))
        (aset new-user strategy "name" (.. profile -name)))
      ; Copy name
      (aset new-user "name" (aget new-user strategy "name"))

      (if (not= (.-emails profile) js/undefined)
        (do (aset new-user strategy "email" (.-value (first (.-emails profile))))
            (aset new-user "email" (.-value (first (.-emails profile))))))

      (if (not= (.-photos profile) js/undefined)
        (do (aset new-user strategy "photo" (.-value (first (.-photos profile))))
            (aset new-user "photo" (.-value (first (.-photos profile))))))

      (-> new-user
          (give-api-token)
          (add-uid (get-api-token new-user))) ; use token as salt for uuid5

      (.save new-user
        (fn [err]
          (if err
            (js/throw err)
            (done nil new-user)))))))

(defn config-passport [passport config-auth user-model]
  ; =========================================================================
  ; passport session setup ==================================================
  ; =========================================================================
  ; required for persistent login sessions
  ; passport needs ability to serialize and unserialize users out of session

  ; used to serialize the user for the session
  (.serializeUser passport (fn [user done] (done nil (.-id user))))

  ; used to deserialize the user
  (.deserializeUser
    passport
    (fn [id done] (.findById user-model id (fn [err user] (done err user)))))

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
        (.findOne
          user-model
          (clj->js {:uid uid})
          (fn [err user]
            ; if there are any errors, return the error before anything else
            (if err
              (done err)
              ; if no user is found, return the message
              (if user
                (if (validate-api-token user token)
                  ; all is well, return successful user
                  (done nil user)
                  ; create the loginMessage and save it to session as flashdata
                  (done nil false (.flash req "loginMessage" "Oops! Wrong token.")))
                ; req.flash is the way to set flashdata using connect-flash
                (done nil false  (.flash req "loginMessage" "No user found."))))))))

  (def-strategy-cb "facebook" config-auth user-model)
  (def-strategy-cb "google" config-auth user-model)))
