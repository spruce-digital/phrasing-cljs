(ns phrasing.router
  (:require [reagent.core]
            [kee-frame.core :refer [switch-route]]
            [phrasing.pages.search :as search-page]))

(def routes
  [["/" search-page/route-key]
   ["/admin" :admin]
   ["/account" :account]
   ["/library" :library]
   ["/library/dialogues/:id" :dialogue]])

(defn router []
  [switch-route (comp :name :data)
    search-page/route-key [search-page/root]
    nil [:div "Loading..."]])
