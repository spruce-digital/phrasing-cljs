(ns phrasing.ui.core
  (:require [reagent.core]
            [garden.core :refer [css]]
            [phrasing.ui.css :refer [defstyle style] :as css]
            [phrasing.ui.values :as v]
            [phrasing.subs :as subs]
            [phrasing.events :as e]
            [kee-frame.core :as k]
            [re-frame.core :as rf]))

;; -- Global Styles ----------------------------------------

(defstyle ::global
  ["body" "html" {:margin 0
                  :padding 0
                  :background (v/color :background)
                  :font :mono}]
  ["a" {:text-decoration :none
        :color (v/color :link)}])

;; -- Detail -----------------------------------------------

(defn detail-item-visual
  "A visual to represent the detail-item places alongside the
  main info"
  [image-or-icon]
  [:div {:style {:width :30px :height :30px :border-radius :15px :background :blue}}])

(defn detail-item-info
  "An icon/value pair to be rendered within the info of a detail item"
  [icon value]
  [:div.item-info-pair
   [:i {:class (str "far fa-" (name icon))}]
   [:span value]])

(defn detail-item
  "A row to be rendered within a detail component. Accepts many
  standard varaibles and lays them out appropriately"
  [{:keys [title image image-ratio icon language listens]}]
  [:article.item
   (when (or image icon)
    [detail-item-visual (or image icon)])
   [:main.rows
    [:div.top-row
     [:h4 title]]
    [:div.bottom-row
     (when language [:span.code (str "@" language)])
     (when listens [detail-item-info :headphones-alt listens])]]])


(defn detail
  "Renders a component that contains a title and action button
  followed by a list of clickable items."
  [{:keys [title action-icon action items]}]
  [:section (style ::detail)
   [:header
    [:h3 title]
    [:i {:class (str "far fa-" (name action-icon))}]]
   [:main
    (for [item items]
     [detail-item item])]])

(defstyle ::detail
  ["&"      {}]
  ["header" {:display :flex
             :flex-direction :row
             :justify-content :space-between
             :align-items :center}])

;; -- Navigation -------------------------------------------

(defn navigation []
  (let [is-authed? @(rf/subscribe [::subs/is-authed?])
        route-key @(rf/subscribe [::subs/route-key])
        logo-href (k/path-for [(if is-authed? :search :home)])]

    [:section (style ::navigation)
     [:span.title
      [:a.logo {:href logo-href} "Phrasing.app"]]
     (if is-authed?
       (if (= route-key :home)
         [:ul.actions
          [:a {:href (k/path-for [:search])}
           [:button.go-to-app "Go to app ➜"]]]
         [:ul.actions
          [:a {:href (k/path-for [:library])}
           [:button.library "Library"]]
          [:a {:href "#"}
           [:button.sign-out {:on-click #(rf/dispatch [::e/sign-out])} "Sign Out"]]])
       [:ul.actions
        [:a {:href "/signin"}
         [:button.sign-in "Sign In"]]
        [:a {:href "/signup"}
         [:button.sign-up "Sign Up"]]])]))

(defstyle ::navigation
  ["&"      {:background    (v/color :card)
             :display       :flex
             :align-items   :center
             :padding       "0px 24px"
             :font          :mono
             :box-shadow    (v/default :box-shadow)}]
  [".title" {:color         (v/color :text)
             :flex          1
             :font-size     "36px"
             :font          :archer}]
  [".logo"  {::css/snippets [:link]}]
  [".actions"  {}
   ["button"   {:button :glass}
    ["&:hover" {:cursor :pointer}]]]
  [".sign-up"  {:button :action}
   ["&:hover"  {:cursor :pointer}]])

;; -- Flash ------------------------------------------------

(defn flash []
  (let [flash @(rf/subscribe [::subs/flash])]
    [:section (style ::flash)
      (when flash
        [:div.flash {:class (-> flash first name)} (last flash)])]))

(defstyle ::flash
  ["&"        {:max-width "600px"
               :margin "0 auto"}]
  [".flash"   {:border-width "1px"
               :border-style :solid
               :padding "12px"
               :margin-top "24px"
               :border-radius (v/default :border-radius)
               :text-align :center}]
  [".error"   {:border-color (v/nord :red)
               :color (v/nord :red)
               :background (v/adjust (v/nord :red) :alpha 0.2)}]
  [".success" {:border-color (v/nord :green)
               :color (v/nord :green)
               :background (v/adjust (v/nord :green) :alpha 0.2)}])

;; -- Layout -----------------------------------------------

(defn layout [& children]
  [:section (style ::layout)
   [navigation]
   [:div.content-container
     (flash)
     (into [:div.content] children)]])

(defstyle ::layout
  ["&"        {::css/snippets [:flex-column]
               :min-height "100vh"}]
  [".content" {::css/snippets [:flex-column]
               :color         (v/color :text)
               :flex-center   :column}])

;; -- Translation ------------------------------------------

(defn translation
  ([tr] [translation :div tr])
  ([tag {:keys [id text language]}]
   [tag (style ::translation)
    [:span.tag (str "@" (language :code) " ")]
    [:span.text text]]))

(defstyle ::translation
  ["&"        {:width "100%"
               :text-align :left}]
  [".tag"     {:color (v/color :bang)}])
