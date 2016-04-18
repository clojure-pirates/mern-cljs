(defproject mern-cljs-example-api "0.1.1-SNAPSHOT"
  :description "MERN-Cljs Example WWW"
  :url "https://github.com/daigotanaka/mern-cljs"
  :license {:name "MIT License"
            :url ""}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ; Added by Daigo
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [com.cognitect/transit-cljs "0.8.220"]
                 [com.cemerick/piggieback "0.2.1"]
                 [com.cemerick/url "0.1.1"]
                 [cljs-ajax "0.5.3"]
                 [net.polyc0l0r/hasch "0.2.3"]
                 [cljs-hash "0.0.2"]
                 ]

  :npm {:dependencies [[express "latest"]
                       [xmlhttprequest "*"]
                       [xmldom "0.1.19"]
                       [source-map-support "*"]
                       [react "0.13.3"]
                       ; Added by Daigo
                       [cors "2.7.1"]
                       [passport "latest"]
                       [passport-local "latest"]
                       [passport-facebook "latest"]
                       [passport-google-oauth "latest"]
                       [aws-sdk "2.3.3"]
                       [mongoose "4.4.12"]
                       [vogels "2.2.0"]
                       [express-session "1.13.0"]
                       [bcrypt-nodejs "0.0.3"]
                       [connect-flash "0.1.1"]
                       [morgan  "1.7.0"]
                       [body-parser  "1.15.0"]
                       [cookie-parser  "1.4.1"]
                       [connect-flash "~0.1.1"]
                       [amqplib "latest"]
                       [when "latest"]
                       ]
        :root :root}

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-npm "0.6.1"]]

  :min-lein-version "2.1.2"

  :hooks [leiningen.cljsbuild]

  :aliases {"start" ["npm" "start"]}

  :main "main.js"

  :source-paths ["src"]

  :clean-targets ^{:protect false} [[:cljsbuild :builds :api :compiler :output-to]
                                    :target-path :compile-path]

  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]
             :server-logfile "logs/figwheel.log"}

  :cljsbuild {:builds
               {
               :api
               {:source-paths ["src"]
                :compiler {:target :nodejs
                           :output-to "main.js"
                           :output-dir "target"
                           :main api.core
                           :figwheel true
                           :optimizations :none}
                }}}

  :profiles {:dev
             {:plugins
              [[lein-figwheel "0.3.9"]]
              :cljsbuild
              {:builds
               {:api
                {:compiler {:pretty-print true}
                 :source-map true
                 :figwheel {:heads-up-display false}}}}
              :npm {:dependencies [[ws "*"]]}}

             :prod
             {:env {:production true}
              :cljsbuild
              {:builds
               {:server
                {:compiler {:optimizations :simple
                            :foreign-libs [{:file "src/polyfill/simple.js"
                                            :provides ["polyfill.simple"]}]
                            :pretty-print false}}}}}})
