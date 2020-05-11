(ns phrasing.ui.css
  (:require [reagent.core]
            [garden.core :refer [css]]
            [clojure.walk :refer [walk]]))

;; DOM Management -------------------------------------------------------------

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

(defn render-styles
  "Creates or updates Stylesheet"
  [styles id]
  (if-let [el (.getElementById js/document id)]
    (update-styles styles el)
    (insert-styles styles id)))

;; Processors -----------------------------------------------------------------

(def snippets {:flex-column {:display :flex
                           :flex-direction :column
                           :flex 1}})

(defn snippet-processor
  "Return a reduced list of mixed in values"
  [rule value styles]
  (reduce #(merge %1 (get snippets %2 {})) {} value))

(def processors
  {::snippets snippet-processor})

(defn process-rule
  "Apply the given processors to a style rule"
  [rule value block]
  (let [processor (get processors rule #(assoc {} rule value))]
    (processor rule value block)))

(defn process-block
  "Apply the given processes to a style block"
  [block]
  (reduce (fn [memo [k v]] (merge memo (process-rule k v block))) {} block))

(defn process-styles
  "Automatically processes styles"
  [styles]
  (into []
    (map #(cond (sequential? %1) (process-styles %1)
                (map? %1) (process-block %1)
                true %1)
         styles)))

;; Helpers --------------------------------------------------------------------

(defn kw->id
  "Converts a keyword to a valid html id"
  [kw]
  (-> kw str (subs 1)
      (clojure.string/replace "." "-")
      (clojure.string/replace "/" "--")))

(defn scope-styles
  "Adds a scope to Stylesheet if required"
  [kw id styles]
  (if (-> kw name (= "global"))
    styles
    (into [(str "." id)] styles)))

;; API ------------------------------------------------------------------------

(defn defstyle
  "Defines and scopes Stylesheet before rendering it"
  [kw & styles]
  (let [id            (kw->id kw)
        scoped-styles (scope-styles kw id styles)]
     (render-styles (-> scoped-styles process-styles css) id)))

(defn style
  "Add class for style to component.
  Optionally can accept props as well"
  ([kw] {:class (kw->id kw)})
  ([kw opts] (update opts :class #(-> kw kw->id (str " " %)))))
