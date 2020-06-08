(ns phrasing.ui.core
  (:require [reagent.core]
            [garden.core :refer [css]]
            [phrasing.ui.css :refer [defstyle style] :as css]
            [phrasing.ui.values :as v]
            [phrasing.subs :as subs]
            [phrasing.events :as e]
            [kee-frame.core :as k]
            [re-frame.core :as rf]))

;; Global Styles -------------------------------------------

(defstyle ::global
  ["body" "html" {:margin 0
                  :padding 0
                  :background (v/color :background)
                  :font :mono}]
  ["a" {:text-decoration :none
        :color (v/color :link)}])

;; Navigation ----------------------------------------------

(defn navigation []
  (let [is-authed? @(rf/subscribe [::subs/is-authed?])
        route-key @(rf/subscribe [::subs/route-key])
        logo-href (k/path-for [(if is-authed? :search :home)])]
    [:section (style ::navigation)
     [:span.title
      [:a.logo {:href logo-href} "Phrasing.app"]]
     (if is-authed?
       [:ul.registration
        (if (= route-key :home)
          [:a {:href (k/path-for [:search])}
           [:button.go-to-app "Go to app ➜"]]
          [:a {:href "#"}
           [:button.sign-out {:on-click #(rf/dispatch [::e/sign-out])} "Sign Out"]])]
       [:ul.registration
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
  [".sign-out" {:button :glass}
   ["&:hover"  {:cursor :pointer}]]
  [".sign-in" {:button :glass}
   ["&:hover" {:cursor :pointer}]]
  [".sign-up" {:button :action}
   ["&:hover" {:cursor :pointer}]]
  [".go-to-app" {:button :glass}
   ["&:hover"   {:cursor :pointer}]])

;; Flash ---------------------------------------------------

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

;; Layout --------------------------------------------------

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
