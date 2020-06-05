(ns phrasing.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [ajax.core :as ajax]
            [kee-frame.core :as kee]))

(defn gq
  ([query] (gq query {}))
  ([query vars]
   (->> {:query query :variables vars}
        (clj->js)
        (.stringify js/JSON))))

(defn gql->http-xhrio [{gql :gql :as context}]
  (assoc context :http-xhrio {:method          :post
                              :uri             "https://localhost:4000/api"
                              :headers         {"Content-Type" "application/json"}
                              :format          (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})}))

(def gql
  (re-frame.core/->interceptor
    :id     :gql
    :after (fn [context]
             (update context :effects gql->http-xhrio))))

(reg-event-fx ::sign-in
  [gql]
  (fn [cofx [_ input]]
    {:http-xhrio {:method          :post
                  :uri             "http://localhost:4000/api"
                  :body            (gq "mutation($input:SessionInput!){signIn(input:$input){token}}" {:input input})
                  :headers         {"Content-Type" "application/json"}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::sign-in-success]
                  :on-failure      [::sign-in-failure]}
     :gql {:op    :mutation
           :defs  {:input "SessionInput!"}
           :args  {:input input}
           :query "signIn(input:$input) { token }"}}))

(reg-event-db ::sign-in-failure
  (fn [db event]
    (println "failure")
    (println db)
    (println event)
    db))

(reg-event-db ::sign-in-success
  (fn [db event]
    (println "success")
    (println db)
    (println event)
    db))


