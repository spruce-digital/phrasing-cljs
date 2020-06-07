(ns phrasing.gql
  (:require [re-frame.core :refer [reg-event-fx ->interceptor]]
            [ajax.core :as ajax]))

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

(defn suffix-keyword
  "Append a string to the end of a keyword, preserving the namespace
  and returning a new keyword"
  [kw suffix]
  (keyword (namespace kw) (str (name kw) suffix)))

(defn suffix-event
  "Appends a suffix to an event name, i.e. :success or :failure"
  [event suffix]
  (let [formatted-suffix (str "-" (name suffix))]
    (update event 0 #(suffix-keyword %1 formatted-suffix))))

(defn success-event [event] (suffix-event event :success))
(defn failure-event [event] (suffix-event event :failure))

;; All GraphQL responses are 200 responses. Check if there is an :errors key in
;; the response, and dispatch :success or :failure events accordingly
(reg-event-fx ::success
  (fn [_cofx [_success req-event res-data]]
    (if (res-data :errors)
      {:dispatch (conj (failure-event req-event) res-data)}
      {:dispatch (conj (success-event req-event) res-data)})))

;; Failed GraphQL events means a non-200 response. Handle these differently than
;; a failued query
(reg-event-fx ::failure
  (fn [cofx event]
    (comment "Network failure and/or server error")
    (println "::failure")
    (println cofx)
    (println event)))

(defn gql->http-xhrio
  "Takes the gql constructor from the context and assocs the appropriate
  http-xhrio data needed to submit it as a request. Parses a gql query
  and fires success/failure based on the original event name. Accepts a
  hash of custom config options to override or append any values"
  [{gql :gql event :event :as effects} config]
  (let [http-xhrio {:method          :post
                    :uri             "http://localhost:4000/api"
                    :headers         {"Content-Type" "application/json"}
                    :body            (construct-gql-data gql)
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})}]
    (assoc effects :http-xhrio (conj http-xhrio config))))

(def gql
  (->interceptor
    :id     :gql
    :after  (fn [context]
             (let [on-success [::success (get-in context [:coeffects :event])]
                   ; on-failure (-> context (get-in [:coeffects :event]) failure-event)]
                   on-failure [::failure (get-in context [:coeffects :event])]]
              (update context :effects #(gql->http-xhrio %1 {:on-success on-success
                                                             :on-failure on-failure}))))))

; (println
;   (construct-gql-data {:op    :mutation
;                        :defs  {:input "SessionInput!"}
;                        :vars  {:input {:email "email" :password "password"}}
;                        :query "signIn(input:$input) { token }"}))
