(ns phrasing.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [phrasing.gql :refer [gql]]
            [kee-frame.core :as kee]))

(reg-event-fx ::sign-out
  (fn [{db :db} _]
    {:db (-> db (dissoc :auth)
                (assoc :flash [:success "Sign out successful"]))
     :navigate-to [:home]}))

(def query-sign-in "
  signIn(input: $input) {
    token
    user { id email }
  }")

(reg-event-fx ::sign-in
  [gql]
  (fn [cofx [_ input]]
    {:gql {:op    :mutation
           :defs  {:input "SessionInput!"}
           :vars  {:input input}
           :query query-sign-in}}))

(reg-event-db ::sign-in-failure
  (fn [db event]
    (assoc db :flash [:error "Invalid credentials"])))

(reg-event-fx ::sign-in-success
  (fn [{db :db} [_ _ res]]
    {:db (-> db (assoc :flash [:success "Sign in successful!"])
                (assoc :auth {:token (-> res :data :signIn :token)})
                (assoc :user (-> res :data :signIn :user)))
     :navigate-to [:search]}))

(def query-sign-up "
  signUp(input: $input) {
    token
    user { id email }
  }")

(reg-event-fx ::sign-up
  [gql]
  (fn [cofx [_ input]]
    {:gql {:op    :mutation
           :defs  {:input "SessionInput!"}
           :vars  {:input input}
           :query query-sign-up}}))

(reg-event-db ::sign-up-failure
  (fn [db event]
    (assoc db :flash [:error "Invalid credentials"])))

(reg-event-fx ::sign-up-success
  (fn [{db :db} [_ _ res]]
    {:db (-> db (assoc :flash [:success "Sign up successful!"])
                (assoc :auth {:token (-> res :data :signIn :token)})
                (assoc :user (-> res :data :signIn :user)))
     :navigate-to [:search]}))

