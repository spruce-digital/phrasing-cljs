(ns phrasing.pages.search
  (:require [reagent.core]
            [phrasing.ui.core :as ui]
            [cljs.pprint :refer [pprint]]
            [ajax.core :as ajax]
            [kee-frame.core :as kee]))

(def route-key :search)

(defn gq
  ([query] (gq query {}))
  ([query vars]
   (->> {:query query :variables vars}
        (clj->js)
        (.stringify js/JSON))))

(kee/reg-controller :search
                    {:params (constantly true)
                     :start  [:search/load]})

(kee/reg-chain :search/load

               (fn [ctx [query]]
                 {:http-xhrio {:method          :post
                               :uri             "http://localhost:4000/api"
                               :body            (gq "query { phrases { id }}")
                               :headers         {"Content-Type" "application/json"}

                               :format          (ajax/json-request-format)
                               :response-format (ajax/json-response-format {:keywords? true})}})

               (fn [{:keys [db]} [_ res]]
                 {:db (assoc db :phrases (-> res :data :phrases))}))

(defn root []
  [ui/layout
    [:div "Search Page"]])

