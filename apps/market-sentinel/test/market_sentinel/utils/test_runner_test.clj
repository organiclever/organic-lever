(ns market-sentinel.utils.test-runner-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [market-sentinel.utils.test-runner :as test-runner]))

(deftest setup-test
  (testing "Setup should create the necessary directory and script file"
    (let [loc-prefix (str "test/tmp/" (.toString (java.util.UUID/randomUUID)))
          bin-dir    (str loc-prefix "/bin")
          bin-kaocha (str bin-dir "/kaocha")]
      (test-runner/setup loc-prefix)

      (is (.exists (io/file bin-kaocha)))
      (is (.exists (io/file bin-dir)))

      (io/delete-file bin-kaocha :silently true)
      (io/delete-file bin-dir :silently true)
      (io/delete-file loc-prefix :silently true))))
