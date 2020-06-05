(ns phrasing.pages.registration
 (:require [reagent.core :as r]
           [re-frame.core :refer [dispatch]]
           [phrasing.events :as e]
           [phrasing.ui.css :refer [defstyle style]]
           [phrasing.ui.values :as v]
           [phrasing.ui.core :as ui]
           [phrasing.ui.form :as form]))

(def sign-in-route ::sign-in)
(def sign-up-route ::sign-up)

(defn sign-up []
  (ui/layout
   [:h1 "Sign Up"]))


(defn sign-in []
  (let [data (r/atom {})]
    (ui/layout
     [:section (style ::registration)
      [:h1 "Sign In"]
      [form/text data :email]
      [form/pass data :password]
      [:button {:type :submit
                :on-click #(dispatch [::e/sign-in @data])} "Sign In"]
      [:span.switch-reg
       "Don't have an account? "
       [:a {:href "/signup"} "Sign up"]
       " here"]])))

(defstyle ::registration
  ["&"            {:card :padded
                   :max-width "600px"}]
  ["button"       {:button :action
                   :width "100%"
                   :margin "12px"}
   ["&:hover"     {:cursor :pointer}]])

