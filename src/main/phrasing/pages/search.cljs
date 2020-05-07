(ns phrasing.pages.search
  (:require [reagent.core]
            [cljs.pprint :refer [pprint]]
            [ajax.core :as ajax]
            [kee-frame.core :as kee]))

(def route-key :search)

(kee/reg-controller :search
                    {:params (constantly true)
                     :start  [:search/load]})

(kee/reg-chain :search/load

               (fn [ctx [query]]
                 {:http-xhrio {:method          :post
                               :uri             "http://localhost:4000/api"
                               :params          {:query "{items{name}}"
                                                 :variables nil}
                               :format          (ajax/json-request-format)
                               :response-format (ajax/json-response-format {:keywords? true})}})

               (fn [{:keys [db]} [_ res]]
                 {:db (assoc db :items (-> res :data :items))}))

(defn root []
  [:div "Search Page"])

