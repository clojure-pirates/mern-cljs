(defproject mern-cljs-example-common "0.1.1-SNAPSHOT"
  :description "MERN-Cljs Example Common files"
  :url "https://github.com/daigotanaka/mern-cljs"
  :license {:name "MIT License"
            :url ""}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ; Added by Daigo
                 [com.cognitect/transit-cljs "0.8.220"] 
                 [com.cemerick/piggieback "0.2.1"]
                 [com.cemerick/url "0.1.1"]
                 [cljs-ajax "0.5.3"]
                 [net.polyc0l0r/hasch "0.2.3"]
                 ]

  :npm {:dependencies [[express "4.13.3"]
                       [xmlhttprequest "*"]
                       [xmldom "0.1.19"]
                       [source-map-support "*"]
                       ; Added by Daigo
                       [passport "~0.1.17"]
                       [passport-local "~0.1.6"]
                       [passport-facebook "~1.0.2"]
                       [mongoose "~3.8.1"]
                       [express-session "~1.0.0"]
                       [bcrypt-nodejs "latest"]
                       [connect-flash "~0.1.1"]
                       [morgan  "~1.0.0"]
                       [body-parser  "~1.0.0"]
                       [cookie-parser  "~1.0.0"]
                       ]
        :root :root}

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-npm "0.6.1"]]

  :min-lein-version "2.1.2"

  :hooks [leiningen.cljsbuild]

  :source-paths ["src"]

  :clean-targets ^{:protect false} [[:cljsbuild :builds :compiler :output-to]
                                    "node_modules"
                                    :target-path :compile-path]

  :cljsbuild {:builds
              [
               {:source-paths ["src"]
                :compiler {:target :nodejs
                           :output-to "main.js"
                           :output-dir "target"
                           :optimizations :none
                           :pretty-print true}
                }]})
