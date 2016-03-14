(defproject mern-cljs-example-www "0.1.1-SNAPSHOT"
  :description "MERN-Cljs Example WWW"
  :url "https://github.com/daigotanaka/mern-cljs"
  :license {:name "MIT License"
            :url ""}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [reagent "0.5.1"]
                 [kioo "0.4.1"]
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
                       [react "0.13.3"]
                       ; Added by Daigo
                       [cors "latest"]
                       [passport "latest"]
                       [passport-local "latest"]
                       [passport-facebook "latest"]
                       [passport-google-oauth "latest"]
                       [mongoose "latest"]
                       [express-session "latest"]
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

  :aliases {"start" ["npm" "start"]}

  :main "main.js"

  :source-paths ["src"]

  :clean-targets ^{:protect false} [[:cljsbuild :builds :backend :compiler :output-to]
                                    [:cljsbuild :builds :frontend :compiler :output-dir]
                                    :target-path :compile-path]

  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]
             :server-logfile "logs/figwheel.log"}

  :cljsbuild {:builds
               {
               :frontend
               {:source-paths ["src/frontend"]
                :compiler {:output-to "resources/public/js/out/app.js"
                           :output-dir "resources/public/js/out"
                           :asset-path "js/out"
                           :main app.start
                           :optimizations :none}}

               :backend
               {:source-paths ["src/backend"]
                :compiler {:target :nodejs
                           :output-to "main.js"
                           :output-dir "target"
                           :main server.core
                           :figwheel true
                           :optimizations :none}
                }}}

  :profiles {:dev
             {:plugins
              [[lein-figwheel "0.3.9"]]
              :cljsbuild
              {:builds
               {:frontend
                {:compiler {:pretty-print true}
                 :source-map true
                 :figwheel true}
                :backend
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
                            :foreign-libs [{:file "src/backend/polyfill/simple.js"
                                            :provides ["polyfill.simple"]}]
                            :pretty-print false}}
                :app
                {:compiler {:output-dir "target/app/out"
                            :optimizations :advanced
                            :pretty-print false}}}}}})
