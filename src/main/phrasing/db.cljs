(ns phrasing.db
  (:require [cljs.spec.alpha :as s]))

;; -- Spec --------------------------------------
;;
;; cljs.spec definition for the database
;;
;; TODO:
;; add an after check to ensure spec validation whenever db is updated

(s/def ::flash (or nil? (s/tuple #{:error :success} string?)))

(s/def ::db (s/keys :opt-un [::flash]))

;; -- Default app-db value ----------------------

(def default-db
  {:flash nil})
