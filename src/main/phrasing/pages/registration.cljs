(ns phrasing.pages.registration
 (:require [reagent.core :as r]
           [re-frame.core :refer [dispatch]]
           [kee-frame.core :as k]
           [phrasing.events :as e]
           [phrasing.ui.css :refer [defstyle style]]
           [phrasing.ui.values :as v]
           [phrasing.ui.core :as ui]
           [phrasing.ui.form :as form]))

;; -- Components -------------------------------------------

(defn form
  "Form responsible for capturing registration information and
  dispatching the provided event. Renders a custom footer at the bottom"
  [label event footer]
  (let [data (r/atom {})]
   [:section (style ::form)
    [:h1 label]
    [form/text data :email]
    [form/pass data :password]
    [:button {:type :submit
              :on-click #(dispatch [event @data])}
      label]
    footer]))

(defn footer
  "Footer used to switch between sign in and sign up.
  Expected to be rendered withing a ::form style block"
  [anchor]
  [:span.switch-reg "Don't have an account? " anchor " here"])

;; -- Root -------------------------------------------------

(defn root-sign-up []
  (ui/layout
    (form "Sign Up" ::e/sign-up
      (footer [:a {:href (k/path-for [:sign-in])} "Sign in"]))))

(defn root-sign-in []
  (ui/layout
    (form "Sign In" ::e/sign-in
      (footer [:a {:href (k/path-for [:sign-up])} "Sign up"]))))

;; -- Styles -----------------------------------------------

(defstyle ::form
  ["&"            {:card :padded
                   :max-width "600px"
                   :box-sizing :border-box}]
  ["button"       {:button :action
                   :width "100%"
                   :margin "12px"}
   ["&:hover"     {:cursor :pointer}]])

