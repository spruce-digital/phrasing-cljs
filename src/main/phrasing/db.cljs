(ns phrasing.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :refer [includes?]]))

;; -- Spec --------------------------------------
;;
;; cljs.spec definition for the database
;;
;; TODO:
;; add an after check to ensure spec validation whenever db is updated

;; types
(s/def ::token (and string? #(-> %1 count (= 343))))
(s/def ::id integer?)
(s/def ::email (and string? #(includes? %1 "@")))

;; flash
(s/def ::flash (s/tuple #{:error :success} string?))

;; auth
(s/def ::auth (s/keys :req-un [::token]))

;; user
(s/def ::user (s/keys :req-un [::id ::email]))

;; app-db
(s/def ::db (s/keys :opt-un [::flash ::auth ::user]))


;; -- Default app-db value ----------------------

(def default-db
  {})
