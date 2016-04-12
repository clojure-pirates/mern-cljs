(ns mern-utils.passport.macros)

(defmacro def-strategy-cb
  "Macro define a strategy callback"
  [strategy config-auth user-model api-token-model social-account-model]
  `(do
     (defn ~(symbol (str strategy "-strategy-callback")) [~'token ~'refreshToken ~'profile ~'done]
       ; asynchronous
       (.nextTick
         nodejs/process
         (fn []
           (~'get-user-from-social-account-id
             ~user-model ~social-account-model (.-id ~'profile)
             (fn [~'err ~'user]
               (if ~'err
                 (~'done ~'err)
                 (if ~'user
                   (~'refresh-api-token ~api-token-model (.-uid ~'user) (fn [~'api-token] (~'done nil ~'user)))
                   (~'register-new-user ~strategy
                                        ~user-model ~api-token-model ~social-account-model
                                        (:email-domain-restriction ~config-auth)
                                        ~'token ~'profile ~'done))))))))
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
