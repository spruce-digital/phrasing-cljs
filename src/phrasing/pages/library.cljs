(ns phrasing.pages.library
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [phrasing.ui.core :as ui]
            [phrasing.ui.values :as v]
            [phrasing.ui.css :refer [defstyle style]]
            [phrasing.subs :as subs]))

;; -- Components -------------------------------------------

(defn dialogues []
  (let [dialogues @(rf/subscribe [::subs/dialogues])
        items     [{:icon :dog
                    :title "About my dog"
                    :language :fr
                    :listens 20}]]
    [:section (style ::dialogues)
     [ui/detail {:title "Dialogues"
                 :action-icon :plus
                 :action #(js/alert "new")
                 :items items}]]))


;; -- Root Component ---------------------------------------

(defn root []
  [ui/layout
   [:section (style ::root)
    [dialogues]]])

;; -- Styles -----------------------------------------------

(defstyle ::root
  ["&"      {}])
