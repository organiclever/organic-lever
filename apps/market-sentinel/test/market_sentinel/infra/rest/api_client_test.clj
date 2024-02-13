(ns market-sentinel.infra.rest.api-client-test
  (:require [clojure.test :refer [deftest is testing]]
            [market-sentinel.infra.rest.api-client :refer [gen-call-rest]]))

(defn mock-method [url config]
  {:url    url
   :config config})

(def base-url "http://localhost")
(def base-config {:headers {"Content-Type" "application/json"}})

(deftest gen-call-rest-test
  (testing "gen-call-rest macro produce the right function for 2 args"
    (let [result ((gen-call-rest base-url base-config)
                  mock-method
                  "api/test")]
      (is (= "http://localhost/api/test" (:url result)))
      (is (= {:headers {"Content-Type" "application/json"}} (:config result)))))
  (testing "gen-call-rest macro produce the right function for 3 args"
    (let [result ((gen-call-rest base-url base-config)
                  mock-method
                  "api/test"
                  {:params {:id 1}})]
      (is (= "http://localhost/api/test" (:url result)))
      (is (= {:headers      {"Content-Type" "application/json"}
              :params       {:id 1}
              :query-params nil} (:config result))))))
