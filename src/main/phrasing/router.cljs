(ns phrasing.router
  (:require [reagent.core]
            [kee-frame.core :refer [switch-route]]
            [phrasing.pages.search :as search]
            [phrasing.pages.registration :as reg]))

(def routes
  [["/" search/route]
   ["/signin" reg/sign-in-route]
   ["/signup" reg/sign-up-route]
   ["/admin" :admin]
   ["/account" :account]
   ["/library" :library]
   ["/library/dialogues/:id" :dialogue]])

(defn router []
  [switch-route (comp :name :data)
    search/route [search/root]
    reg/sign-in-route [reg/sign-in]
    reg/sign-up-route [reg/sign-up]
    nil [:div "Loading..."]])
