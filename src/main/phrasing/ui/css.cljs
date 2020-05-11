(ns phrasing.ui.css
  (:require [reagent.core]
            [garden.core :refer [css]]))

(defn insert-styles
  "Inserts Stylesheet into document head"
  [styles id]
  (let [el (.createElement js/document "style")
        node (.createTextNode js/document styles)]
    (.setAttribute el "id" id)
    (.appendChild el node)
    (.appendChild (.-head js/document) el)
    el))

(defn update-styles
  "Updates Stylesheet when present"
  [styles el]
  (aset el "innerHTML" styles)
  (comment .appendChild el (.createTextNode js/document styles)))

(defn kw->id [kw]
  (-> kw str (subs 1)
      (clojure.string/replace "." "-")
      (clojure.string/replace "/" "--")))

(defn render-stylesheet [styles id]
  (if-let [el (.getElementById js/document id)]
    (update-styles styles el)
    (insert-styles styles id)))

(defn defstyle [kw & styles]
  (let [id              (kw->id kw)
        prefixed-styles (if (-> kw name (= "global"))
                          styles
                          (into [(str "." id)] styles))]
     (render-stylesheet (css prefixed-styles) id)))

(defn style
  ([kw] {:class (kw->id kw)})
  ([kw opts] (update opts :class #(-> kw kw->id (str " " %)))))

(def mixins {:flex-column {:display :flex
                           :flex-direction :column
                           :flex 1}})

(defn get-mixin [mixin?]
  (if (map? mixin?) mixin?
                    (get mixins mixin? {})))

(defn mixin [& opts]
  (reduce #(merge %1 (get-mixin %2)) {} opts))
