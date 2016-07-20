(ns mern-utils.aws
  (:require-macros
    [mern-utils.macros :refer [node-require]]))

(node-require fs "fs")
(node-require s3 "s3")

(defn upload-file-to-s3
  [params then on-err]
  "Upload file to S3
  params should contain:
    :path Local path
    :file-name file name
    :s3-bucket
    :s3-path
    :delete true to delete the local file after successful upload
    :aws {:region "" :access-key-id "" :secret-access-key ""}
  params will be passed on to (then params) or (on-err err params)"
  (set! (.. s3 -AWS -config -region) (:region (:aws params)))
  (let [full-path (str (:path params) "/" (:file-name params))
        s3-client (.createClient s3
                    (clj->js {:maxAsyncS3 20 ; this is the default
                              :s3RetryCount 3 ; this is the default
                              :s3RetryDelay 1000 ; this is the default
                              :multipartUploadThreshold 20971520 ; this is the default (20 MB)
                              :multipartUploadSize 15728640 ; this is the default (15 MB)
                              :s3Options {:accessKeyId (:access-key-id (:aws params))
                                          :secretAccessKey (:secret-access-key (:aws params))}}))
        s3-params {:localFile full-path
                :s3Params {:Bucket (:s3-bucket params)
                           :Key (str (:s3-path params) "/" (:file-name params))
                           :ACL "public-read"}}
        uploader (.uploadFile s3-client (clj->js s3-params))]
    (.on uploader "error" (fn[err] (on-err (.-stack err) params)))
    (.on uploader
         "end" (fn[] (do (if (:delete params) (.unlink fs full-path))
                         (then params))))))
