(ns common.config
  (:require
    [mern-utils.lib :refer [local-ip]]))

(def LOCAL-IP "localhost")
;(def LOCAL-IP "0.0.0.0")
; You need to use local-ip instead to access from other devices.
; In that case you also need to add Valid OAuth redirect URIs on Facebook app page
; (def LOCAL-IP local-ip)

(def MONGODB-DOMAIN LOCAL-IP)
(def MONGODB-PORT 27017)
(def MONGODB-DBNAME "merncljs_auth")

(def DYNAMODB-DOMAIN LOCAL-IP)
(def DYNAMODB-PORT 8000)

; mongodb or dynamodb
(def DATABASE "mongodb")
(def DB-ENDPOINT (str "mongodb://" MONGODB-DOMAIN ":" MONGODB-PORT "/" MONGODB-DBNAME))

; (def DATABASE "dynamodb")
; Keep DynamoDB endpoint nil to use AWS (non-local)
; (def DB-ENDPOINT (str "http://" DYNAMODB-DOMAIN ":" DYNAMODB-PORT))

(def RABBITMQ-DOMAIN LOCAL-IP)
(def RABBITMQ-PORT 5672)
(def RABBITMQ-DEFAULT-QUEUE "task_queue")

(def API-DOMAIN LOCAL-IP)
(def API-PORT 5000)

(def WWW-DOMAIN LOCAL-IP)
(def WWW-PORT 1337)

; Enter "yourdomain.com" to limit access
(def EMAIL-DOMAIN-RESTRICTION nil)  

(def PRIMARY-SOCIAL-AUTH "facebook")

(def API-TOKEN-EXPIRES-IN (* 60 60 24 14)) ; 2 weeks

; Those social app accounts are set up for the example app
(def FACEBOOK-CLIENT-ID     "825920487534725")
(def FACEBOOK-CLIENT-SECRET "93bf4835a0b66422e49a480c7be711c5")
(def GOOGLE-CLIENT-ID       "1071376916361-af1hfk0b90bru0gn1esh3stksd0hb1ii.apps.googleusercontent.com")
(def GOOGLE-CLIENT-SECRET   "xxduCYSBUdTx7TfMoxk3hwrB")

(def config-auth
  {:email-domain-restriction EMAIL-DOMAIN-RESTRICTION
   :token-expires-in-sec API-TOKEN-EXPIRES-IN
   :facebook-auth
    {:client-id FACEBOOK-CLIENT-ID
     :client-secret FACEBOOK-CLIENT-SECRET
     :callback-url (str "http://" WWW-DOMAIN ":" WWW-PORT "/authcb/facebook")
     :profile-fields ["id" "displayName" "photos" "email" "name"]
     :enableProof true}
   :google-auth
    {:client-id GOOGLE-CLIENT-ID
     :client-secret GOOGLE-CLIENT-SECRET
     :callback-url (str "http://" WWW-DOMAIN ":" WWW-PORT "/authcb/google")}})

(def cors-options
  (clj->js {:origin (str "http://" LOCAL-IP ":" WWW-PORT)
            :credentials true
            :allowedHeaders "Authorization,Origin,X-Requested-With,Content-Type,Accept"}))
