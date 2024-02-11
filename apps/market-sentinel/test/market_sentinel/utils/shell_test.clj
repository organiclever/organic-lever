(ns market-sentinel.utils.shell-test
  (:require [clojure.test :refer [deftest is testing]]
            [market-sentinel.utils.shell :as shell]))

(deftest exec-sh-cmd-test
  (testing "exec-sh-cmd should execute shell command and return output or error"
    ;; Test a command that should succeed
    (is (= "Hello, World!\n" (shell/exec-sh-cmd "echo Hello, World!")))))
