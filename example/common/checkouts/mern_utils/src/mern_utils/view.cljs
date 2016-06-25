(ns mern-utils.view
  (:require
    [cljs.core.async :as async :refer [chan close! timeout put!]]
    [reagent.core :as reagent]
    [kioo.reagent :refer [html-content content append after set-attr do->
                         substitute listen unwrap]]
    [kioo.core :refer [handle-wrapper]]
    [goog.string :as gstring])
  (:require-macros
    [cljs.core.async.macros :as m :refer [go go-loop alt!]]
    [kioo.reagent :refer [defsnippet deftemplate snippet]]))

(defn html5
  ; hack: strip off react attributes from server side rendering to avoid
  ; the conflict with frontend react id
  [content]
  (str "<!DOCTYPE html>\n"
       (clojure.string/replace
         content
         #"(data-react-checksum|data-reactid)=\"[^\"]*\""
         "")))

(defn render-script [src]
  (str "<script>" src "</script>\n"))

(defn list-meta [meta-data]
  (map #(vector :meta %) meta-data))

(defsnippet page "public/template.html" [:html]
  [data & {:keys [scripts]}]
  {[:head :> :title] (content (:title data))
   [:head] (append (list-meta (:meta-data data)))
   [:body :> :content] (content (:content data))
   [:body] (append [:div (for [src scripts]
                           ^{:key (gstring/hashCode (pr-str src))}
                           [:script
                            (if (string? src)
                              {:dangerouslySetInnerHTML {:__html src}}
                              src)])])})

(def base-scripts [{:src "/js/out/app.js"}])

(defn render-page [data]
  (let [out (chan 1)
        scripts (into [] (concat base-scripts (:scripts data) ["main_cljs_fn()"] ))]
    (go
      (put! out
            (->
              (page data :scripts scripts)
              (reagent/render-to-string)
              (html5))))
    out))
