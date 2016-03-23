(defproject mern-cljs-example-worker "0.1.1-SNAPSHOT"
  :description "MERN-Cljs Example Worker"
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

  :npm {:dependencies [[xmlhttprequest "*"]
                       [source-map-support "*"]
                       ; Added by Daigo
                       [mongoose "latest"]
                       [amqplib "latest"]
                       [when "latest"]]
        :root :root}

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-npm "0.6.1"]]

  :min-lein-version "2.1.2"

  :hooks [leiningen.cljsbuild]

  :aliases {"start" ["npm" "start"]}

  :main "main.js"

  :source-paths ["src"]

  :clean-targets ^{:protect false} [[:cljsbuild :builds :worker :compiler :output-to]
                                    :target-path :compile-path]

  :figwheel {:http-server-root "public"
             :server-logfile "logs/figwheel.log"}

  :cljsbuild {:builds
               {
               :worker
               {:source-paths ["src"]
                :compiler {:target :nodejs
                           :output-to "main.js"
                           :output-dir "target"
                           :main worker.core
                           :figwheel true
                           :optimizations :none}
                }}}

  :profiles {:dev
             {:plugins
              [[lein-figwheel "0.3.9"]]
              :cljsbuild
              {:builds
               {:worker
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
