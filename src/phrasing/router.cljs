(ns phrasing.router
  (:require [reagent.core]
            [kee-frame.core :refer [switch-route]]
            [phrasing.pages.search :as search]
            [phrasing.pages.registration :as reg]
            [phrasing.pages.home :as home]))

(def routes
  [["/" :home]
   ["/search" :search]
   ["/signin" :sign-in]
   ["/signup" :sign-up]
   ["/admin" :admin]
   ["/account" :account]
   ["/library" :library]
   ["/library/dialogues/:id" :dialogue]])

(defn router []
  [switch-route (comp :name :data)
   :home [home/root]
   :search [search/root]
   :sign-in [reg/root-sign-in]
   :sign-up [reg/root-sign-up]
   nil [:div "Loading..."]])
