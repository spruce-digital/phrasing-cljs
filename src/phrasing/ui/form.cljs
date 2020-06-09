(ns phrasing.ui.form
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [phrasing.ui.css :refer [defstyle style kw->id]]
            [phrasing.ui.values :as v]
            [clojure.string :as str]))

;; -- Helpers ----------------------------------------------

(defn parse-translation-input
  "Translations are entered as string but have directives in them
  to describe them linguistically. These are always prefixed with
  an @ sign, and the original input text is stored as :input. The
  expected translation output should be a map of {:input :text :code}.
  This is a very basic initial version and should be abstracted out
  into it's own module"
  [input]
  (let [words   (str/split input #" ")
        init    {:input input :text "" :code nil}
        reducer (fn [memo word]
                  (if (str/starts-with? word "@")
                    (assoc memo :code (subs word 1))
                    (update memo :text #(str/trim (str %1 " " word)))))]
    (reduce reducer init words)))

(update {:text ""} :text str "foo" " ")


(defn kw->label
  "Takes a keyword field name and returns a human readable string
  representation. Words get separated by spaces and each word is
  capitalized. :phrasing.app/field-name becomes Field Name"
  [kw]
  (->> (clojure.string/split (name kw) #"\b")
       (map clojure.string/capitalize)
       (clojure.string/join)))

(defn swap-field!
  "Updates a field in an atom with the value of an event. If a
  function is passed then the value will first fun through the
  function before being added"
  ([src field event] (swap-field! src field event identity))
  ([src field event fn]
   (swap! src assoc field (-> event .-target .-value fn))))

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

(defn tr [data field]
  [:fieldset (style ::fieldset)
    [:input.text
      {:type "text"
       :value (get-in @data [field :input] "")
       :on-change #(swap-field! data field % parse-translation-input)}]
    (when-let [code (get-in @data [field :code])]
      [:div.tag "@" code])])

;; -- Styles -----------------------------------------------

(defstyle ::fieldset
  ["&"            {:border :none
                   :width  "100%"
                   :display :flex
                   :flex-direction :column
                   :padding 0
                   :margin 0
                   :margin-bottom :12px}]
  ["label"        {:font-size "12px"
                   :padding-bottom "4px"}]
  [".text"        {:border :none
                   :background (v/color :field)
                   :padding "12px"
                   :font-size "16px"
                   :color (v/color :text)
                   :border-radius (v/default :border-radius)
                   :font :mono
                   :margin 0}]
  [".tag"         {:color (v/color :bang)
                   :margin-top :6px
                   :margin-left :12px
                   :font-size :12px}])
