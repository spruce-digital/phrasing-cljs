(ns phrasing.ui.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [phrasing.ui.css :refer [defstyle style kw->id]]
            [phrasing.ui.values :as v]))

;; -- Helpers ----------------------------------------------

(defn kw->label [kw]
  (->> (clojure.string/split (name kw) #"\b")
       (map clojure.string/capitalize)
       (clojure.string/join)))

(defn swap-field!
  [src field event]
  (swap! src assoc field (-> event .-target .-value)))

;; -- Methods ----------------------------------------------

(defn just-dispatch
  "Helper function to be used for event handlers. Prevents defaults
  and dispatched the event to re-frame"
  [dom-event rf-event]
  (.preventDefault dom-event)
  (rf/dispatch rf-event))

(defn e-value
  "Helper function to get the target value of a dom event. Used to
  abstract away the unpleasant syntax"
  [dom-event]
  (-> dom-event .-target .-value))

;; -- Fields -----------------------------------------------

(defn text [data field]
  [:fieldset (style ::fieldset)
    [:label {:for (kw->id field)} (kw->label field)]
    [:input.text
      {:type "text"
       :id (kw->id field)
       :value (@data field)
       :on-change #(swap-field! data field %)}]])

(defn pass [data field]
  [:fieldset (style ::fieldset)
    [:label {:for (kw->id field)} (kw->label field)]
    [:input.text
      {:type "password"
       :value (@data field)
       :on-change #(swap-field! data field %)}]])

;; -- Styles -----------------------------------------------

(defstyle ::fieldset
  ["&"            {:border :none
                   :width  "100%"
                   :display :flex
                   :flex-direction :column}]
  ["label"        {:font-size "12px"
                   :padding-bottom "4px"}]
  [".text"        {:border :none
                   :background (v/color :field)
                   :padding "12px"
                   :font-size "16px"
                   :color (v/color :text)
                   :border-radius (v/default :border-radius)
                   :font :mono}])
