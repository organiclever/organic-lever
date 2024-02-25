(ns market-sentinel.stocks.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [market-sentinel.stocks.config :refer [calculate-healthy-pe]]))

(deftest calculate-healthy-pe-test
  (testing "calculate-healthy-pe should return healthy PE"
    (is (= 15 (calculate-healthy-pe 20 25))))
  (testing "calculate-healthy-pe should return nil when either argument is nil."
    (is (= nil (calculate-healthy-pe nil 25)))
    (is (= nil (calculate-healthy-pe 20 nil)))
    (is (= nil (calculate-healthy-pe nil nil)))))