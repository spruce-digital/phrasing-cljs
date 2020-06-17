(ns phrasing.pages.library
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [phrasing.ui.core :as ui]
            [phrasing.ui.values :as v]
            [phrasing.ui.css :refer [defstyle style]]
            [phrasing.subs :as subs]))

;; -- Components

(defn dialogues []
  (let [dialogues @(rf/subscribe [::subs/dialogues])]
    [:section (style ::dialogues)
     [:div.title-bar
      [:h2 "Dialogues"]
      [:span "Add new"]]
     [:ul.dialogue-list
      (if (empty? dialogues)
        [:div.empty-message
         [:h3 "No dialogues found"]
         [:div "Create a dialogue by clicking the plus sign in the top right"]]
        (for [dialogue dialogues]
          [:li.dialogue
           [:div "icon"]
           [:div "title"]]))]]))


;; -- Root Component ---------------------------------------

(defn root []
  [ui/layout
   [:section (style ::root)
    [dialogues]]])

;; -- Styles -----------------------------------------------

(defstyle ::root
  ["&"      {}])
