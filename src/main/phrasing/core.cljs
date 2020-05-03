(ns phrasing.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [phrasing.events :as event]
            [phrasing.subs :as sub]))

(defn ui
  []
  (let [items @(rf/subscribe [::sub/items])
        _ (println "items" items)]
    [:div
     [:h1 "These are your items"]
     (for [item items]
       ^{:key item} [:li "Name: " (item :name)])]))

;; -- Entry Point -------------------------------------------------------------

(defn render
  []
  (dom/render [ui]
              (js/document.getElementById "root")))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (rf/clear-subscription-cache!)
  (render))

(defn init
  []
  (rf/dispatch-sync [::event/initialize])
  (rf/dispatch-sync [::event/gql-test])
  (render))

