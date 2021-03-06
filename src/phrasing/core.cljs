(ns phrasing.core
  (:require [reagent.core :as r]
            [reagent.dom :as dom]
            [re-frame.core :as rf]
            [phrasing.events :as e]
            [phrasing.subs :as sub]
            [phrasing.router :refer [routes router]]
            [phrasing.db :refer [default-db]]
            [kee-frame.core :as kee]
            [re-frisk.core :as re-frisk]))

(re-frisk/enable)

(defn start-app! []
  (kee/start! {:routes         routes
               :root-component [router]
               :initial-db     default-db
               :app-db-spec    :phrasing.db/db
               :debug?         true})
  (rf/dispatch [::e/init]))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (rf/clear-subscription-cache!)
  (start-app!))

