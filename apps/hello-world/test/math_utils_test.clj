(ns math-utils-test
  (:require [clojure.test :refer [are deftest testing]]
            [math-utils :refer [my-add]]))

(deftest my-add-test
  (testing "my-add"
    (are [x y expected]
         (= expected (my-add x y))
      1 2 3
      4 5 9)))
