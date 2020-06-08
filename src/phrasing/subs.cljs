(ns phrasing.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub ::items
  (fn [db _]
    (:items db)))

(reg-sub ::flash
 (fn [db _]
   (db :flash)))

(reg-sub ::is-authed?
 (fn [db _]
   (-> db :auth :token some?)))

(reg-sub ::route-key
 (fn [db _]
   (some-> db :kee-frame/route :data :name)))

(reg-sub ::search
 (fn [db _]
   (db :search)))
