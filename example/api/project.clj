(defproject mern-cljs-example-api "0.1.1-SNAPSHOT"
  :description "MERN-cljs Example"
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

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-npm "0.6.1"]]

  :min-lein-version "2.1.2"

  :hooks [leiningen.cljsbuild]

  :aliases {"start" ["npm" "start"]}

  :main "js/main.js"

  :source-paths ["src"]

  :target-path "js/target"

  :clean-targets ^{:protect false} [[:cljsbuild :builds :api :compiler :output-to]
                                     :target-path :compile-path]

  :figwheel {:http-server-root "public"
             :css-dirs ["resources/public/css"]
             :server-logfile "logs/figwheel.log"}

  :cljsbuild
  {:builds
   {:api
    {:source-paths ["src"]
     :compiler {:target :nodejs
                :output-to "js/main.js"
                :output-dir "js/target"
                :main api.core}}}}

  :profiles
  {:dev
   {:plugins [[lein-figwheel "0.3.9"]]

    :cljsbuild
    {:builds
     {:api
      {:compiler
       {:pretty-print true
        :optimizations :none
        :source-map true
        :figwheel {:heads-up-display false}
        :npm {:dependencies [[ws "*"]]}}}}}}

   :prod
   {:env {:production true}

    :cljsbuild
    {:builds
     {:api
      {:compiler
       {:optimizations :simple
        :source-map "js/main.js.map"
        :foreign-libs [{:file "src/polyfill/simple.js"
                        :provides ["polyfill.simple"]}]
        :pretty-print false}}}}}})
