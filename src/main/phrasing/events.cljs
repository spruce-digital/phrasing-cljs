(ns phrasing.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(reg-event-db
  ::initialize
  (fn [_ _]
    {:items []}))

(reg-event-db
  ::success
  (fn [db [_ result]]
    (assoc db :items (get-in result [:data :items]))))

(reg-event-db
  ::failure
  (fn [db [_ result]]
    (println "failure" result)
    db))

(reg-event-fx
  ::gql-test
  (fn [_ _]
    {:http-xhrio {:response-format (ajax/json-response-format {:keywords? true})
                  :format          (ajax/json-request-format)
                  :uri             "http://localhost:4000/api"
                  :method          :post
                  :params          {:query "{items{name}}"}
                  :timeout         8000
                  :on-success      [::success]
                  :on-failure      [::failure]}}))


