(defproject mern-utils "0.1.1-SNAPSHOT"
  :description "MERN-Cljs"
  :url "https://github.com/daigotanaka/mern-cljs"
  :license {:name "MIT License"
            :url ""}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [reagent "0.5.1"]
                 [kioo "0.4.1"]
                 [com.cognitect/transit-cljs "0.8.220"] 
                 [com.cemerick/piggieback "0.2.1"]
                 [com.cemerick/url "0.1.1"]
                 [cljs-ajax "0.5.3"]]

  :npm {:dependencies [[express "4.13.4"]
                       [xmlhttprequest "1.8.0"]
                       [xmldom "0.1.22"]
                       [source-map-support "0.4.0"]
                       [react "0.13.3"]
                       [express-session "1.13.0"]
                       [mongoose "4.4.12"]
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
