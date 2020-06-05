(ns phrasing.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [ajax.core :as ajax]
            [kee-frame.core :as kee]))

(defn gql-operation
  "Get the operation (:op) from the gql constructor.
  Will be one of :query, :mutation, or :subscription"
  [gql]
  (-> (get gql :op :query)
      name))

(defn gql-definitions
  "Get the definitions (:defs) from the gql constructor.
  Key value pair of {:name \"Type\"}"
  [gql]
  (->> (get gql :defs {})
       (reduce-kv #(str %1 "$" (name %2) ":" %3 ",") "")))

(defn gql-query
  "Get the query (:query) from the gql constructor.
  Strip leading and trailing curly braces, as they are included
  as part of the query string template."
  [gql]
  (-> (get gql :query "")
      (clojure.string/replace #"^\{?(.*)\}?$" "$1")))

(defn construct-gql-data
  "Convert a gql constructor to a json string ready to be sent to
  the server. Will return {:query query :variables varibles} where
  :query is a fully templated query string and :variables is the :vars
  key in the gql constructor"
  [gql]
  (let [query (str (gql-operation gql) "(" (gql-definitions gql) "){" (gql-query gql) "}")]
    (->> {:query query :variables (gql :vars)}
         (clj->js)
         (.stringify js/JSON))))

(defn gql->http-xhrio
  "Takes the gql constructor from the context and assocs the appropriate
  http-xhrio data needed to submit it as a request"
  [{gql :gql :as context}]
  (assoc context :http-xhrio {:method          :post
                              :uri             "http://localhost:4000/api"
                              :headers         {"Content-Type" "application/json"}
                              :body            (construct-gql-data gql)
                              :format          (ajax/json-request-format)
                              :response-format (ajax/json-response-format {:keywords? true})
                              :on-success      [::sign-in-success]
                              :on-failure      [::sign-in-failure]}))

(def gql
  (re-frame.core/->interceptor
    :id     :gql
    :after (fn [context]
             (update context :effects gql->http-xhrio))))

(reg-event-fx ::sign-in
  [gql]
  (fn [cofx [_ input]]
    {:gql {:op    :mutation
           :defs  {:input "SessionInput!"}
           :vars  {:input input}
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

; (println
;   (construct-gql-data {:op    :mutation
;                        :defs  {:input "SessionInput!"}
;                        :vars  {:input {:email "email" :password "password"}}
;                        :query "signIn(input:$input) { token }"}))
