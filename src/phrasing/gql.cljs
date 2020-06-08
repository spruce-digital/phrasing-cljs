(ns phrasing.gql
  (:require [re-frame.core :refer [reg-event-fx reg-fx ->interceptor]]
            [ajax.core :as ajax]
            [cljs.spec.alpha :as s]))

;; Initial spec of gql
;; TODO: ensure that variables present in :query have matching :vars and :defs
(def def-regex #"^[A-Z][a-zA-Z]*!?$")
(s/def ::def (s/and string? #(re-matches def-regex %)))

(s/def ::op #{:query :mutation :subscription})
(s/def ::defs (s/map-of keyword? ::def))
(s/def ::vars (s/map-of keyword? any?))
(s/def ::query string?)

(s/def ::gql (s/keys :req-un [::query]
                     :opt-un [::op ::defs ::vars]))

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
  (let [query    (gql :query)
        wrapped? (-> query first (= "{"))]
    (if-not wrapped?
      query
      (-> query (clojure.string/replace #"^\{\s*" "")
                (clojure.string/replace #"\s*\}$" "")))))

(defn gql-query-string
  "Get the query string from the gql constructor, including operations,
  definitions, and template"
  [gql]
  (let [operation   (gql-operation gql)
        definitions (gql-definitions gql)
        query       (gql-query gql)]
    (-> (str operation "(" definitions "){" query "}")
        (clojure.string/replace "()" ""))))

(defn construct-gql-data
  "Convert a gql constructor to a json string ready to be sent to
  the server. Will return {:query query :variables varibles} where
  :query is a fully templated query string and :variables is the :vars
  key in the gql constructor"
  [gql]
  (let [query-string (gql-query-string gql)
        variables    (get gql :vars {})]
    (->> {:query query-string :variables variables}
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

(defn update-xhrio-req
  "Accepts an effects object and gql descriptor and updates the :http-xhrio
  effect with the appropriate request information."
  [fx gql]
  (let [http-xhrio {:method          :post
                    :uri             "http://localhost:4000/api"
                    :headers         {"Content-Type" "application/json"}
                    :body            (construct-gql-data gql)
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})}]
    (update fx :http-xhrio merge http-xhrio)))

(defn update-xhrio-handlers
  "Takes an effects map and handlers and updates the :http-xhrio config with the handlers"
  [fx handlers]
  (update fx :http-xhrio merge handlers))

(defn update-xhrio-token
  "Takes and effects map and token, and if the token is present adds the appropriate
  authorization header"
  [fx token]
  (if (clojure.string/blank? token)
    fx
    (let [auth-headers {"Authorization" (str "Bearer " token)}]
      (update-in fx [:http-xhrio :headers] merge auth-headers))))

(defn handle-invalid-gql
  "Print information to the console when a gql descriptor is not valid"
  [event gql]
  (let [prefix (-> event first str)]
    (js/console.error prefix "invalid :gql in event " (-> event clj->js))
    (js/console.error prefix "gql: " (clj->js gql))
    (js/console.error prefix "explain-str: " (s/explain-str ::gql gql))
    (js/console.error prefix "explain-data: " (clj->js (s/explain-data ::gql gql)))))

;; An interceptor is used so we have access to the full context without
;; needing to manually pass it in. Essentially we are just registering
;; an effect with slightly more access.
(def ->gql
  (->interceptor
    :id     :gql
    :after  (fn [ctx]
             (let [event      (get-in ctx [:coeffects :event])
                   gql        (get-in ctx [:effects :gql])
                   token      (some-> ctx :coeffects :db :auth :token)
                   handlers   {:on-success [::success event]
                               :on-failure [::failure event]}
                   no-gql?    (nil? gql)
                   invalid?   (not (s/valid? ::gql gql))]
              (cond
                no-gql?  ctx
                invalid? (handle-invalid-gql event gql)
                :else    (update ctx :effects #(-> %1 (update-xhrio-req gql)
                                                      (update-xhrio-handlers handlers)
                                                      (update-xhrio-token token))))))))


;; Register the :gql effect as a no-op to squelch error messages.
;; gql queries should make use of the gql interceptor
(reg-fx :gql (fn [_]))


; (println
;   (construct-gql-data {:op    :mutation
;                        :defs  {:input "SessionInput!"}
;                        :vars  {:input {:email "email" :password "password"}}
;                        :query "signIn(input:$input) { token }"}))
