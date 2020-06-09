(ns phrasing.pages.phrase
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [kee-frame.core :as k]
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

(def query-create-phrase
  "createPhrase(input: $input) {
    id
    translations {
      text
      language { code }
    }
  }")

(rf/reg-event-fx ::create
  [->gql]
  (fn [cofx [_ data]]
    (let [clean-data (->> data (map #(select-keys % [:text :code]))
                               (remove empty?))]
      {:gql {:query query-create-phrase
             :defs  {:input "PhraseInput"}
             :vars  {:input {:translations clean-data}}
             :op    :mutation}})))

(rf/reg-event-fx ::create-success
  (fn [{db :db} _]
    {:db (assoc db :flash [:success "Phrase successfully created"])
     :navigate-to [:search]}))

(rf/reg-event-db ::create-failure
  (fn [db _]
    (assoc db :flash [:error "Failed to create phrase"])))

;; -- Components -------------------------------------------

(defn show-phrase [phrase on-edit]
  [:section (style ::phrase)
    [:div.title-bar
      [:h1 "Show Phrase"]
      [:span.action {:on-click on-edit}
        "Edit Phrase"]]
    (when phrase
      (for [tr (phrase :translations)]
        ^{:key (tr :id)}
        [:div.tr [ui/translation tr]]))])

(defn edit-phrase [phrase event-name]
  (let [translations (r/atom (get phrase :translations []))]
    (fn [_ _]
      [:section (style ::phrase)
        [:div.title-bar
          [:h1 "Edit Phrase"]
          [:span.action {:on-click #(rf/dispatch [event-name @translations])}
            "Save"]]
        (for [index (-> @translations count range)]
          ^{:key index}
          [form/tr translations index])
        [:button.add-tr {:on-click #(swap! translations conj {})}
          "Add Translation"]])))

;; -- Roots ------------------------------------------------

(defn root-new []
  (let [phrase @(rf/subscribe [::sub/phrase :new])]
    [edit-phrase phrase ::create]))

(defn root-existing [id]
  (rf/dispatch [::fetch id])
  (let [editing? (r/atom false)]
    (fn [id]
      (let [phrase @(rf/subscribe [::sub/phrase id])]
        (if @editing?
          [edit-phrase phrase ::update]
          [show-phrase phrase #(swap! editing? not)])))))

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

(defstyle ::phrase
  ["&"    {:width "100%"
           :max-width (v/default :max-width)
           :margin :24px}]
  [".title-bar" {:display :flex
                 :justify-content :space-between
                 :align-items :flex-end
                 :margin-bottom :12px}
   ["h1"        {:margin 0}]
   [".action"   {:font-size :14px
                 :color (v/color :bang)}
    ["&:hover"  {:cursor :pointer
                 :text-decoration :underline}]]]

  [".tr"  {:card :padded
           :margin 0}]
  [".add-tr"  {:button :action
               :width "100%"}
   ["&:hover" {:cursor :pointer}]])

