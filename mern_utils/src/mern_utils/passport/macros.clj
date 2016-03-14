(ns mern-utils.passport.macros)

(defmacro def-strategy-cb
  "Macro define a strategy callback"
  [strategy config-auth user-model]
  `(do
     (defn ~(symbol (str strategy "-strategy-callback")) [~'token ~'refreshToken ~'profile ~'done]
       ; asynchronous
       (.nextTick
         nodejs/process
         (fn []
           (.findOne
             ~user-model
             (~'clj->js {(keyword (str ~strategy ".id")) (.-id ~'profile)})
             (fn [~'err ~'user]
               (if ~'err
                 (~'done ~'err)
                 (if ~'user
                   (do (-> ~'user (~'give-api-token)
                                (.save (fn [~'err] (if ~'err (js/throw ~'err) true))))
                       (~'done nil ~'user))
                   (~'register-new-user ~strategy ~user-model (:email-domain-restriction ~config-auth) ~'token ~'profile ~'done))))))))
     (if (get-in ~config-auth [(keyword (str ~strategy "-auth")) :client-id])
       (do
         (.use
           ~'passport
           (~(symbol (str strategy "-strategy."))
             (~'clj->js
               {:clientID (get-in ~config-auth [(keyword (str ~strategy "-auth")) :client-id])
                :clientSecret (get-in ~config-auth [(keyword (str ~strategy "-auth")) :client-secret])
                :callbackURL (get-in ~config-auth [(keyword (str ~strategy "-auth")) :callback-url])
                :profileFields (get-in ~config-auth [(keyword (str ~strategy "-auth")) :profile-fields])})
            ~(symbol (str strategy "-strategy-callback"))))
         (println "Registered " ~strategy " strategy.")))))
