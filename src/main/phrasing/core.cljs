(ns phrasing.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [phrasing.events :as event]
            [phrasing.subs :as sub]
            [phrasing.router :refer [routes router]]
            [kee-frame.core :as kee]
            [stylefy.core :as stylefy]
            [re-frisk.core :as re-frisk]))

(re-frisk/enable)

(defn start-app! []
  (stylefy/init)
  (kee/start! {:routes         routes
               :root-component [router]
               ; :initial-db     {:foo :bar}
               :debug?         true}))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (rf/clear-subscription-cache!)
  (start-app!))

