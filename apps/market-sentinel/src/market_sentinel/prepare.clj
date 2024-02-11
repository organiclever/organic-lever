(ns market-sentinel.prepare
  (:require [market-sentinel.utils.shell :refer [exec-sh-cmd]]
            [market-sentinel.utils.test-runner :as test-runner]))

(defn prepare-project
  "prepare-project prepares the project for execution by performing necessary setup tasks."
  [& _args]

  ;; add tmp dir for testing
  (exec-sh-cmd "mkdir -p test/tmp/")
  ;; setup test-runner
  (test-runner/setup nil))

(comment
  (prepare-project))