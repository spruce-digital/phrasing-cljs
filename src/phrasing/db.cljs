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
(s/def ::id string?)
(s/def ::email (and string? #(includes? %1 "@")))

;; flash
(s/def ::flash (s/tuple #{:error :success} string?))

;; auth
(s/def ::auth (s/keys :req-un [::token]))

;; user
(s/def ::user (s/keys :req-un [::id ::email]))

;; languages
(s/def ::code (and string? #(-> %1 count (= 2))))
(s/def ::language (s/keys :opt-un [::code]))

;; translations
(s/def ::translation (s/keys :req-un [::id]
                             :opt-un [::language]))
(s/def ::translations (s/* ::translation))

;; search
(s/def :phrasing.db.search/suggestions (s/* ::translation))
(s/def :phrasing.db.search/query string?)
(s/def ::search (s/keys :req-un [:phrasing.db.search/suggestions :phrasing.db.search/query]))

;; phrases
(s/def ::phrase (s/keys :req-un [::id]
                        :opt-un [::translations]))
(s/def ::phrases (s/* ::phrase))

;; ready
(s/def ::ready boolean?)

;; app-db
(s/def ::db (s/keys :opt-un [::flash ::auth ::user ::phrases]
                    :req-un [::ready ::search]))


;; -- Default app-db value ----------------------

(def default-db
  {:ready false
   :search {:query ""
            :suggestions []}})

;; -- LocalStorage -----------------------------------------
;;
;; Store data into LocalStorage and be able to read it back. This
;; data should be a whitelisted representation of the app-db with
;; the same (albeit incomplete) structure.
;;
;; Loading of the LocalStorage data is stored as a coeffects handler.
;; This is per the recommendation of the re-frame docs, although
;; alternatives have not been explored.
;;
;; Saving of the LocalStorage data is done via an interceptor. To ensure
;; that the latest copy of db is used, dispatch the :phrasing.events/save-to-ls
;; by way of the :dispatch fx

(def ls-key "phrasing.app")

(defn set-ls-data
  "Save a map of data to LocalStorage. Stored as an EDN string
  so it can easily converted back to a map by the reader."
  [data]
  (.setItem js/localStorage ls-key (str data)))

(defn get-ls-data
  "Retrieve the string stored in LocalStorage and convert back
  to data with the reader. Use the some->> macro to error early
  and if nothing is stored. Always return a map"
  []
  (into {} (some->> (.getItem js/localStorage ls-key)
                    (cljs.reader/read-string))))

(defn db->ls
  "Takes a db state and returns a whitelisted map with the data
  to store in LocalStorage"
  [db]
  (select-keys db [:auth :user]))

(re-frame.core/reg-cofx
  :load-ls
  (fn [cofx _]
    (assoc cofx :ls (get-ls-data))))

(def update-ls
  (re-frame.core/->interceptor
    :id    :update-ls
    :after (fn [ctx]
            (-> ctx :coeffects :db db->ls set-ls-data)
            ctx)))
