(ns phrasing.pages.search
  (:require [reagent.core :as r]
            [kee-frame.core :as k]
            [phrasing.ui.core :as ui]
            [phrasing.ui.css :refer [defstyle style]]
            [phrasing.ui.values :as v]
            [phrasing.ui.form :as form]
            [phrasing.gql :refer [->gql]]
            [phrasing.subs :as sub]
            [cljs.pprint :refer [pprint]]
            [ajax.core :as ajax]
            [re-frame.core :as rf]))

;; -- Data -------------------------------------------------

(def query-phrases
  "phrases {
    id
    translations {
      id
      text
      language {
        code
      }
    }
  }")

(rf/reg-event-fx ::fetch
  [->gql]
  (fn [_ _] {:gql {:query query-phrases}}))

(rf/reg-event-db ::fetch-success
  (fn [db [_ res]]
    (->> (-> res :data :phrases)
         (reduce #(assoc %1 (%2 :id) %2) {})
         (assoc db :phrases))))

(reduce #(assoc %1 (%2 :id) %2)
        {}
        [{:id 1 :foo :bar} {:id 2 :baz :qux}])

(rf/reg-event-db ::fetch-failure
  (fn [db [_ res]]
    (assoc db :flash [:error "Failed to fetch phrases"])))

(def query-suggestions
  "translations(query: $query) {
    id
    text
    language { code }
  }")

(rf/reg-event-fx ::query
  [->gql]
  (fn [{db :db} [_ input]]
    (if (= input "")
      {:db (assoc db :search {:query "" :suggestions []})}
      {:db (assoc-in db [:search :query] input)
       :gql {:query query-suggestions
             :defs  {:query "String"}
             :vars  {:query input}}})))

(rf/reg-event-db ::query-success
  (fn [db [_ _ res]]
    (assoc-in db [:search :suggestions] (-> res :data :translations))))

(rf/reg-event-db ::query-failure
  (fn [db _]
    (assoc db :flash [:error "Error fetching suggestions"])))

;; -- Compnents --------------------------------------------

(defn suggestions []
  (let [{:keys [query suggestions]} @(rf/subscribe [::sub/search])]
    (when-not (empty? suggestions)
     [:ul.suggestions
       (for [sugg suggestions]
         ^{:key (sugg :id)}
         [ui/translation :li sugg])
       [:li
        [:span.tag "Add phrase "]
        query]])))

(defn search-bar []
  (let [focused? (r/atom false)]
   (fn []
    (let [{:keys [query]} @(rf/subscribe [::sub/search])
          on-change #(rf/dispatch [::query (form/e-value %)])]
      [:form (style ::search-bar {:on-submit #(form/just-dispatch % [::submit])})
        [:input.search {:placeholder "search..."
                        :value query
                        :on-change on-change
                        :on-focus #(reset! focused? true)
                        :on-blur #(reset! focused? false)}]
        (when @focused? [suggestions])]))))

(defn phrase-card [phrase]
  [:a {:href (k/path-for [:phrase phrase]) :style {:color :inherit}}
    [:article (style ::phrase)
     (for [tr (phrase :translations)]
      ^{:key (tr :id)} [ui/translation tr])]])

(defn phrase-list []
  (let [phrases @(rf/subscribe [::sub/phrases])]
    [:section.phrase-list
      (if (empty? phrases)
        [:h4 "Start by adding a phrase"]
        (for [phrase phrases]
          ^{:key (phrase :id)} [phrase-card phrase]))]))

;; -- Root -------------------------------------------------

(defn root []
  (rf/dispatch [::fetch])
  (fn []
    [ui/layout
     [:section (style ::root)
      [search-bar]
      [phrase-list]]]))


;; -- Styles -----------------------------------------------

(defstyle ::root
  ["&" {:padding-top "24px"
        :width "100%"
        :max-width (v/default :max-width)}]

  [".search-bar" {:padding "12px 24px"
                  :font :mono}])

(defstyle ::search-bar
  [".search" {:padding "12px 24px"
              :font :mono
              :background (v/color :field)
              :border :none
              :font-size "18px"
              :color (v/color :text)
              :width "100%"
              :box-sizing :border-box
              :border-radius (v/default :border-radius)
              :transition "all 100ms ease-in"}
   ["&:active" "&:focus" {:background (v/color :field-active)}]]
  [".suggestions" {:list-style-type :none
                   :background (v/color :field)
                   :padding 0
                   :margin 0
                   :margin-top "12px"
                   :border-radius (v/default :border-radius)
                   :overflow :hidden}
    ["li"         {:padding "12px 12px"
                   :border-bottom (str "1px solid" (v/nord :black-))}
     ["&:hover"   {:background (v/color :field-active)
                   :cursor :pointer}]
     [".tag"      {:color (v/color :bang)}]
     ["&:last-child" {:border-bottom :none}]]])

(defstyle ::phrase
  ["&"      {:card :default
             :margin 0
             :margin-top :12px
             :padding :24px
             :box-sizing :border-box}]
  [".tr" {:width "100%"
          :text-align :left}
   [".tag" {:color (v/color :bang)}]])
