(ns phrasing.pages.phrase
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [phrasing.gql :refer [->gql]]
            [phrasing.ui.css :refer [defstyle style]]
            [phrasing.ui.values :as v]
            [phrasing.ui.form :as form]
            [phrasing.ui.core :as ui]
            [phrasing.subs :as sub]))

;; -- Data -------------------------------------------------

(def query-phrase
  "phrase(id: $id) {
    id
    translations {
      id
      text
      language { code }
    }
  }")

(rf/reg-event-fx ::fetch
  [->gql]
  (fn [cofx [_ id]]
    {:gql {:query query-phrase
           :defs  {:id "ID"}
           :vars  {:id id}}}))

(rf/reg-event-db ::fetch-success
  (fn [db [_ id res]]
    (assoc-in db [:phrases id] (-> res :data :phrase))))

(rf/reg-event-db ::fetch-failure
  (fn [db _]
    (assoc db :flash "Error fetching phrase")))

;; -- Roots ------------------------------------------------

(defn root-new []
  (let [phrase @(rf/subscribe [::sub/phrase :new])]
    [:h1 "new-phrase"]))

(defn root-existing [id]
  (rf/dispatch [::fetch id])
  (fn [id]
    (let [phrase @(rf/subscribe [::sub/phrase id])]
      [:section (style ::root)
        [:h1 "Show Phrase"]
        (when phrase
          (for [tr (phrase :translations)]
            ^{:key (tr :id)}
            [:div.tr [ui/translation tr]]))])))

;; -- Handler ----------------------------------------------
;;
;; Due to limirations with kee-frame, restful routes are not allowed.
;; This limitation goes back to Reitit router not allowing conflicting
;; routes (for example, /phrase/:id conflicts with /phrase/new).
;; In an attempt to circumvent this, all prhase routes will be coalesced
;; to a single root component, which will deicde what to render.
;; Perhaps this will be replaced into the future, or worked back into
;; kee-frame.
;;
;; Three states are needed to be supported: show, edit, and new. The first
;; two can manage on the same route with a numeric ID, where the third will
;; can use an id of "new". What the root component can do is:
;;   1. Layout the page
;;   2. Figure out which phrase to read from app-db
;;   3. Render a phrase component with the appropriate state

(defn handler []
  (let [phrase-id @(rf/subscribe [::sub/path-param :id])]
    [ui/layout
     (if (= phrase-id "new") [root-new] [root-existing phrase-id])]))

;; -- Styles -----------------------------------------------

(defstyle ::root
  ["&"    {:width "100%"
           :max-width (v/default :max-width)}]
  [".tr"  {:card :padded
           :margin 0}])
