(defproject mern-utils "0.1.1-SNAPSHOT"
  :description "MERN-Cljs"
  :url "https://github.com/daigotanaka/mern-cljs"
  :license {:name "MIT License"
            :url ""}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [reagent "0.5.1"]
                 [kioo "0.4.1"]
                 [com.cognitect/transit-cljs "0.8.220"]
                 [com.cemerick/piggieback "0.2.1"]
                 [com.cemerick/url "0.1.1"]
                 [cljs-ajax "0.5.3"]
                 [net.polyc0l0r/hasch "0.2.3"]]

  :npm {:dependencies [[amqplib "0.4.2"]
                       [aws-sdk "2.3.3"]
                       [bcrypt-nodejs "0.0.3"]
                       [body-parser  "1.15.0"]
                       [bunyan "1.8.1"]
                       [connect-flash "0.1.1"]
                       [cookie-parser  "1.4.1"]
                       [cors "2.7.1"]
                       [express "4.13.4"]
                       [express-session "1.13.0"]
                       [joi "8.0.5"]
                       [mongoose "4.4.12"]
                       [morgan  "1.7.0"]
                       [passport "0.3.2"]
                       [passport-facebook "2.1.1"]
                       [passport-google-oauth "1.0.0"]
                       [passport-local "1.0.0"]
                       [react "0.13.3"]
                       [source-map-support "0.4.0"]
                       [vogels "2.2.0"]
                       [when "3.7.7"]
                       [xmldom "0.1.22"]
                       [xmlhttprequest "1.8.0"]
                       ]
        :root :root}

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-npm "0.6.1"]
            [lein-doo "0.1.6"]]

  :min-lein-version "2.1.2"

  :hooks [leiningen.cljsbuild]

  :source-paths ["src"]

  :clean-targets ^{:protect false} [[:cljsbuild :builds :compiler :output-to]
                                    "node_modules"
                                    :target-path :compile-path]

  :doo {:build "test"}

  :cljsbuild {:builds
              [
               {:id "prod"
                :source-paths ["src"]
                :compiler {:target :nodejs
                           :output-to "main.js"
                           :output-dir "target"
                           :optimizations :none
                           :pretty-print true}}
               {:id "test"
                :source-paths ["src" "test"]
                :compiler {:target :nodejs
                           :output-to "resources/public/js/testable.js"
                           :main mern-utils.test.runner
                           :optimizations :none}}
               ]})
