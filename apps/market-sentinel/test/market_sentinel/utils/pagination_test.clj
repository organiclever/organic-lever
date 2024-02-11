(ns market-sentinel.utils.pagination-test
  (:require [clojure.test :refer [are deftest is testing]]
            [market-sentinel.utils.pagination :as pagination :refer [fetch-paginated-data]]))

(deftest get-more?-test
  (testing "fetch-more? will return true if start-at + max-results < total"
    (are [start-at max-results total expected]
         (= expected (pagination/get-more? start-at max-results total))
      0 50 193 true
      50 50 193 true
      100 50 193 true
      0 100 101 true))

  (testing "fetch-more? will return false if start-at + max-results >= total"
    (are [start-at max-results total expected]
         (= expected (pagination/get-more? start-at max-results total))
      150 50 193 false
      0 50 50 false
      1 100 101 false)))

(deftest fetch-paginated-data-test
  (testing "fetch-paginated-data function"
    (let [test-data [{:total-items        193
                      :fetch-items-called 2}
                     {:total-items        40
                      :fetch-items-called 1}
                     {:total-items        283
                      :fetch-items-called 3}]]
      (dorun
       (map
        (fn [{:keys [total-items fetch-items-called]}]
          (let [counter          (atom 0)
                mock-fetch-items (fn [start-at max-results]
                                   (swap! counter inc)
                                   {:startAt    start-at
                                    :total      total-items
                                    :maxResults max-results
                                    :items      (range start-at (min total-items (+ start-at max-results)))})
                result           (fetch-paginated-data mock-fetch-items)]
            (is (= total-items (count result)))
            (is (= (range 0 total-items) result))
            (is (= fetch-items-called @counter))))
        test-data)))))
