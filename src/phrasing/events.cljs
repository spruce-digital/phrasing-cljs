(ns phrasing.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]
            [phrasing.gql :refer [->gql]]
            [phrasing.db :refer [update-ls]]
            [kee-frame.core :as kee]))

;; -- Initialization ---------------------------------------

(reg-event-fx ::init
  [(inject-cofx :load-ls)]
  (fn [{:keys [db ls]}]
    {:db (merge db ls)}))

(reg-event-db ::save-to-ls
  [update-ls]
  (fn [db _] db))

;; -- Sign out ---------------------------------------------

(reg-event-fx ::sign-out
  (fn [{db :db} _]
    {:db (-> db (dissoc :auth)
                (assoc :flash [:success "Sign out successful"]))
     :navigate-to [:home]
     :dispatch [::save-to-ls]}))

;; -- Sign in ----------------------------------------------

(def query-sign-in "
  signIn(input: $input) {
    token
    user { id email }
  }")

(reg-event-fx ::sign-in
  [->gql]
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
     :navigate-to [:search]
     :dispatch [::save-to-ls]}))

;; -- Sign up ----------------------------------------------

(def query-sign-up "
  signUp(input: $input) {
    token
    user { id email }
  }")

(reg-event-fx ::sign-up
  [->gql]
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
                (assoc :auth {:token (-> res :data :signUp :token)})
                (assoc :user (-> res :data :signUp :user)))
     :navigate-to [:search]
     :dispatch [::save-to-ls]}))

;; -- Suggestions ------------------------------------------

(reg-event-fx ::new-phrase-from-search
  (fn [{db :db} [_ input]]
    {:db (assoc db :new-phrase {:translations [{:input input}]})
     :navigate-to [:phrase {:id "new"}]}))
