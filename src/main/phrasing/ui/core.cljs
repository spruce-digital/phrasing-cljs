(ns phrasing.ui.core
  (:require [reagent.core]
            [garden.core :refer [css]]
            [phrasing.ui.css :refer [defstyle style] :as css]
            [kee-frame.core :as kee]))

(defstyle ::global
  ["body" "html" {:margin 0
                  :padding 0}])

(defstyle ::navigation
  ["&"      {:background :orange}]
  [".title" {:color :blue}])

(defn navigation []
  [:section (style ::navigation)
   [:span.title "Phrasing"]])

(defstyle ::layout
  ["&"        {::css/snippets [:flex-column]
               :min-height "100vh"}]
  [".content" {::css/snippets [:flex-column]
               :background :grey}])

(defn layout [& children]
  [:section (style ::layout)
   [navigation]
   (into [:div.content] children)])
