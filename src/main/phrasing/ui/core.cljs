(ns phrasing.ui.core
  (:require [reagent.core]
            [garden.core :refer [css]]
            [phrasing.ui.css :refer [defstyle style mixin]]
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
  ["&"        (mixin :flex-column
                {:min-height "100vh"})]
  [".content" (mixin :flex-column
                {:background :grey})])

(defn layout [& children]
  [:section (style ::layout)
   [navigation]
   (into [:div.content] children)])
