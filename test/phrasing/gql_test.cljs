(ns phrasing.gql-test
  (:require [cljs.test :refer-macros [deftest is run-tests testing]]
            [phrasing.gql :as gql]))

(deftest gql-query
  (testing "Strips surrounding curly braces"
    (is (= "phrases { id }"
           (gql/gql-query {:query "{phrases { id }}"}))))
  (testing "Strips intermediate white space"
    (is (= "phrases { id }"
           (gql/gql-query {:query "{ phrases { id } }"}))))
  (testing "Leaves an unwrapped query's end braces"
    (is (= "phrases { id }"
           (gql/gql-query {:query "phrases { id }"})))))

(deftest gql-query-string
  (testing "Builds simple queries"
    (is (= "query{phrases { id }}"
           (gql/gql-query-string {:query "phrases { id }"}))))
  (testing "Builds typed mutations"
    (is (= "mutation($t:Type,){phrases { id }}"
           (gql/gql-query-string {:query "phrases { id }"
                                  :op    :mutation
                                  :defs  {:t "Type"}})))))

(run-tests)
