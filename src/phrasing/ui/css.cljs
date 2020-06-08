(ns phrasing.ui.css
  (:require [reagent.core]
            [garden.core :refer [css]]
            [phrasing.ui.values :as v]
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
                             :flex 1}
               :link {:text-decoration :none
                      :color           :inherit}})

(def buttons {:default {:border        :none
                        :font          :mono
                        :font-size     "14px"
                        :padding       "12px 24px"
                        :border-radius (v/default :border-radius)}
              :glass   {:background :none
                        :color      (v/color :bang)}
              :action  {:background (v/color :logo)
                        :color      (v/color :background)}})

(def flex-centers {:default {:display :flex
                             :align-items :center}
                   :column  {:flex-direction :column}})

(def fonts {:default {}
            :archer  {:font-family "'Baveuse', serif"}
            :mono    {:font-family "'Operator Mono', monospace"}
            :sans    {:font-family "'Avenir Next', sans-serif"}})

(def cards {:default {:background     (v/color :card)
                      :border-radius  (v/default :border-radius)
                      :box-shadow     (v/default :box-shadow)
                      :display        :flex
                      :flex-direction :column
                      :align-items    :center
                      :margin         "24px"
                      :width          "100%"}
            :padded {:padding "24px"
                     :box-sizing :border-box}})

(declare process-block)

(defn process
  [values key]
  (process-block
    (merge (values :default {}) (values key {}))))

(def processors
  {::snippets (fn [rule value styles] ( -> #(merge %1 (get snippets %2 {}))
                                           (reduce {} value)))
   :button      #(process buttons %2)
   :flex-center #(process flex-centers %2)
   :font        #(process fonts %2)
   :card        #(process cards %2)})

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
