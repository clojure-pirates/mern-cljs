(ns common.config
  (:require
    [cljs.nodejs :as nodejs]
    [mern-utils.backend-lib :refer [local-ip create-logger]]))

(def WWW-SITE-TITLE "MERN-cljs Example")
(def PROD-WWW-DOMAIN "your-domain.com")
(def DEFAULT-LOGIN-PAGE "me")

; "mongodb" or "dynamodb"
(def DATABASE "mongodb")

(def MONGODB-DOMAIN "localhost")
(def MONGODB-PORT 27017)
(def MONGODB-DBNAME "merncljs_auth")

(def RABBITMQ-DEFAULT-QUEUE "task_queue")

(def PRIMARY-SOCIAL-AUTH "facebook")

(def API-TOKEN-EXPIRES-IN (* 60 60 24 14)) ; 14 days

(def LOGGER-CONFIG
  {:name "mern-cljs-example"
   :streams [{:level "info" :path "./logs/mern-cljs-example.log"}
             {:level "trace" :stream (.. nodejs/process -stdout)}
             ]})

; Probably you can leave the configs below as they are

(def IS-PRODUCTION (if (= (.. nodejs/process -env -NODE_ENV) "production" ) true false))

(def LOCAL-IP (if IS-PRODUCTION "0.0.0.0" "localhost"))

(def COOKIE-SECRET (if IS-PRODUCTION (.. nodejs/process -env -COOKIE_SECRET)
                                     "very very secret"))

(def DYNAMODB-DOMAIN LOCAL-IP)
(def DYNAMODB-PORT 7893)

(def DB-ENDPOINT
  (case DATABASE
    "mongodb" (str "mongodb://" MONGODB-DOMAIN ":" MONGODB-PORT "/" MONGODB-DBNAME)
    "dynamodb" (if IS-PRODUCTION nil (str "http://" DYNAMODB-DOMAIN ":" DYNAMODB-PORT))))

(def USE-RABBITMQ true)
(def RABBITMQ-DOMAIN (if IS-PRODUCTION (.. nodejs/process -env -RABBITMQ_DOMAIN) LOCAL-IP))
(def RABBITMQ-PORT (if IS-PRODUCTION (.. nodejs/process -env -RABBITMQ_PORT) 5672))
(def RABBITMQ-USER (if IS-PRODUCTION (.. nodejs/process -env -RABBITMQ_USER) ""))
(def RABBITMQ-PASSWORD (if IS-PRODUCTION (.. nodejs/process -env -RABBITMQ_PASSWORD) ""))

(def API-DOMAIN LOCAL-IP)
(def API-PORT 5000)

(def WWW-DOMAIN (if IS-PRODUCTION "0.0.0.0" LOCAL-IP))
; Cannot use 80 inside docker container. So 1337 internally and map the port to host
(def WWW-PORT 1337)

(def WWW-EXTERNAL-DOMAIN (if IS-PRODUCTION PROD-WWW-DOMAIN (str WWW-DOMAIN ":" WWW-PORT)))

; Enter "yourdomain.com" to limit access
(def EMAIL-DOMAIN-RESTRICTION nil)

; Those social app accounts are set up for the example app
(def FACEBOOK-CLIENT-ID     (if IS-PRODUCTION (.. nodejs/process -env -FACEBOOK-CLIENT-ID)
                                              "825920487534725"))
(def FACEBOOK-CLIENT-SECRET (if IS-PRODUCTION (.. nodejs/process -env -FACEBOOK-CLIENT-SECRET)
                                              "93bf4835a0b66422e49a480c7be711c5"))

(def GOOGLE-CLIENT-ID     (if IS-PRODUCTION (.. nodejs/process -env -FACEBOOK-CLIENT-ID)
                            "1071376916361-af1hfk0b90bru0gn1esh3stksd0hb1ii.apps.googleusercontent.com"))
(def GOOGLE-CLIENT-SECRET (if IS-PRODUCTION (.. nodejs/process -env -FACEBOOK-CLIENT-SECRET)
                            "xxduCYSBUdTx7TfMoxk3hwrB"))

(def config-auth
  {:email-domain-restriction EMAIL-DOMAIN-RESTRICTION
   :token-expires-in-sec API-TOKEN-EXPIRES-IN
   :facebook-auth
    {:client-id FACEBOOK-CLIENT-ID
     :client-secret FACEBOOK-CLIENT-SECRET
     :callback-url (str "http://" WWW-EXTERNAL-DOMAIN "/authcb/facebook")
     :profile-fields ["id" "displayName" "photos" "email" "name"]
     :enableProof true}
   :google-auth
    {:client-id GOOGLE-CLIENT-ID
     :client-secret GOOGLE-CLIENT-SECRET
     :callback-url (str "http://" WWW-EXTERNAL-DOMAIN "/authcb/google")}})

(def cors-options
  (clj->js {:origin (str "http://" WWW-EXTERNAL-DOMAIN)
            :credentials true
            :allowedHeaders "Authorization,Origin,X-Requested-With,Content-Type,Accept"}))

(def AWS-ACCESS-KEY-ID (.. nodejs/process -env -AWS_ACCESS_KEY_ID))
(def AWS-SECRET-ACCESS-KEY (.. nodejs/process -env -AWS_SECRET_ACCESS_KEY))
(def AWS-REGION (.. nodejs/process -env -AWS_REGION))
(def AWS-CONFIG {:accessKeyId AWS-ACCESS-KEY-ID
                 :secretAccessKey AWS-SECRET-ACCESS-KEY
                 :region AWS-REGION})

(def RABBITMQ-ENDPOINT
  (if IS-PRODUCTION
    (str "amqp://" RABBITMQ-USER ":" RABBITMQ-PASSWORD "@" RABBITMQ-DOMAIN "/" RABBITMQ-USER)
    (str "amqp://" RABBITMQ-DOMAIN ":" RABBITMQ-PORT)))

(def LOGGER
  (create-logger (clj->js LOGGER-CONFIG)))
