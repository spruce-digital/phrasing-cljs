(ns phrasing.pages.search
  (:require [reagent.core :as r]
            [phrasing.ui.core :as ui]
            [phrasing.ui.css :refer [defstyle style]]
            [phrasing.ui.values :as v]
            [phrasing.ui.form :as form]
            [phrasing.gql :refer [->gql]]
            [cljs.pprint :refer [pprint]]
            [ajax.core :as ajax]
            [re-frame.core :as rf]))

;; -- Data -------------------------------------------------

(def query-phrases
  "phrases {
    id
    translations {
      id
      text
      language {
        code
      }
    }
  }")

(rf/reg-event-fx ::fetch
  [->gql]
  (fn [_ _] {:gql {:query query-phrases}}))

(rf/reg-event-db ::fetch-success
  (fn [db [_ res]]
    (assoc db :phrases (-> res :data :phrases))))

(rf/reg-event-db ::fetch-failure
  (fn [db [_ res]]
    (assoc db :flash [:error "Failed to fetch phrases"])))

;; -- Compnents --------------------------------------------

(defn search-bar []
  (let [data (r/atom {})]
    [:form (style ::search-bar {:on-submit #(form/dispatch % [::submit @data])})
      [form/formatted data :input {:placeholder "search..."}]]))

;; -- Root -------------------------------------------------

(defn root []
  (rf/dispatch [::fetch])
  [ui/layout
   [:section (style ::root)
    (search-bar)]])

;; -- Styles -----------------------------------------------

(defstyle ::root
  ["&" {:padding-top "24px"
        :width "100%"
        :max-width (v/default :max-width)}]

  [".search-bar" {:padding "12px 24px"
                  :font :mono}])

(defstyle ::search-bar
  [".formatted" {:padding "12px 24px"
                 :font :mono
                 :background (v/color :field)
                 :border :none
                 :font-size "18px"
                 :color (v/color :text)
                 :width "100%"
                 :box-sizing :border-box
                 :border-radius (v/default :border-radius)}])


