(ns phrasing.router
  (:require [reagent.core]
            [kee-frame.core :refer [switch-route]]
            [phrasing.pages.search :as search]
            [phrasing.pages.registration :as reg]
            [phrasing.pages.home :as home]
            [phrasing.pages.phrase :as phrase]
            [phrasing.pages.library :as library]))

(def routes
  [["/" :home]
   ["/search" :search]
   ["/signin" :sign-in]
   ["/signup" :sign-up]
   ["/admin" :admin]
   ["/account" :account]
   ["/library" :library]
   ["/library/dialogues/:id" :dialogue]
   ["/phrase/:id" :phrase]])

(defn router []
  [switch-route (comp :name :data)
   :home [home/root]
   :search [search/root]
   :sign-in [reg/root-sign-in]
   :sign-up [reg/root-sign-up]
   :phrase [phrase/handler]
   :library [library/root]
   nil [:div "Loading..."]])
