(ns phrasing.ui.core
  (:require [reagent.core]
            [garden.core :refer [css]]
            [phrasing.ui.css :refer [defstyle style] :as css]
            [phrasing.ui.values :as v]
            [phrasing.subs :as subs]
            [kee-frame.core :as kee]
            [re-frame.core :as rf]))

;; Global Styles ---------------------------------------------------------------

(defstyle ::global
  ["body" "html" {:margin 0
                  :padding 0
                  :background (v/color :background)
                  :font :mono}]
  ["a" {:text-decoration :none
        :color (v/color :link)}])

;; Navigation ------------------------------------------------------------------

(defn navigation []
  [:section (style ::navigation)
   [:span.title
    [:a.logo {:href "/"} "Phrasing"]]
   [:ul.registration
    [:a {:href "/signin"}
     [:button.sign-in "Sign In"]]
    [:a {:href "/signup"}
     [:button.sign-up "Sign Up"]]]])

(defstyle ::navigation
  ["&"      {:background    (v/color :card)
             :display       :flex
             :align-items   :center
             :padding       "0px 24px"
             :font          :mono
             :box-shadow    (v/default :box-shadow)}]
  [".title" {:color         (v/color :logo)
             :flex          1
             :font-size     "36px"
             :font          :archer}]
  [".logo"  {::css/snippets [:link]}]
  [".sign-in" {:button :glass}
   ["&:hover" {:cursor :pointer}]]
  [".sign-up" {:button :action}
   ["&:hover" {:cursor :pointer}]])

;; Notifications ---------------------------------------------------------------

(defn notifications []
  (let [errors (rf/subscribe [::subs/errors])]
    [:section (style ::notifications)
      (when (count @errors)
        [:div.errors])]))

(defstyle ::notifications
  ["&"        {}]
  [".errors"  {:border-color (str "1px solid " (v/color :red))
               :color        (v/color :red)
               :padding      "12px"}])

;; Layout ----------------------------------------------------------------------

(defn layout [& children]
  [:section (style ::layout)
   [navigation]
   [:div.content-container
     (notifications)
     (into [:div.content] children)]])

(defstyle ::layout
  ["&"        {::css/snippets [:flex-column]
               :min-height "100vh"}]
  [".content" {::css/snippets [:flex-column]
               :color         (v/color :text)
               :flex-center   :column}])
