(ns market-sentinel.stocks.eod-test
  (:require [clojure.test :refer [deftest is testing]]
            [market-sentinel.stocks.eod :as eod]))

(deftest get-avg-growth-test
  (testing "get-growth should return the correct growth for positive case"
    (let [eods   [{:adjusted-close 100}
                  {:adjusted-close 10}]
          growth (eod/get-avg-growth eods)]
      (is (= 900 growth))))
  (testing "get-growth should return the correct growth for negative case"
    (let [eods   [{:adjusted-close 10}
                  {:adjusted-close 100}]
          growth (eod/get-avg-growth eods)]
      (is (= -90 growth)))))

