(ns market-sentinel.utils.pretty-spit-test
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [market-sentinel.utils.pretty-spit :refer [pretty-spit-edn pretty-spit-json]]))

(deftest pretty-spit-edn-test
  (testing "pretty-spit-edn produce the correct output"
    (let [f-path "test/tmp/pretty_spitted_test.edn"
          a-coll {:a 1
                  :b "some-string"}]
      (pretty-spit-edn f-path a-coll)
      (is (= a-coll
             (edn/read-string (slurp f-path)))))))

(deftest pretty-spit-json-test
  (testing "pretty-spit-json-produce the correct output"
    (let [f-path "test/tmp/pretty_spitted_test.json"
          a-coll {:a 1
                  :b "some-string"}]
      (pretty-spit-json f-path a-coll)
      (is (=
           a-coll
           (json/read-str (slurp f-path) :key-fn keyword))))))