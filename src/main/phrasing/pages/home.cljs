(ns phrasing.pages.home
  (:require [reagent.core :as r]
            [phrasing.ui.css :refer [defstyle style]]
            [phrasing.ui.core :as ui]))

(defn root []
  (ui/layout
   [:h1 "Home page"]))
