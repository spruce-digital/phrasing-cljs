(ns phrasing.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [phrasing.gql :refer [gql]]
            [kee-frame.core :as kee]))

(reg-event-fx ::sign-in
  [gql]
  (fn [cofx [_ input]]
    {:gql {:op    :mutation
           :defs  {:input "SessionInput!"}
           :vars  {:input input}
           :query "signIn(input:$input) { token }"}}))

(reg-event-db ::sign-in-failure
  (fn [db event]
    (assoc db :flash [:error "Invalid credentials"])))

(reg-event-db ::sign-in-success
  (fn [db event]
    (assoc db :flash [:success "Sign in successful!"])))

