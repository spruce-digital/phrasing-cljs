(ns phrasing.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  ::items
  (fn [db _]
    (:items db)))
