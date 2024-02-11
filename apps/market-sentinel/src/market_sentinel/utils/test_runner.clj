(ns market-sentinel.utils.test-runner
  (:require [clojure.java.io :as io]
            [market-sentinel.utils.shell :refer [exec-sh-cmd]]))

(defn setup [location]
  (let [loc-prefix (if location (str location "/") "")]
    (exec-sh-cmd (str "mkdir -p " loc-prefix "bin"))
    (with-open [writer (io/writer (str loc-prefix "bin/kaocha"))]
      (.write writer "#!/usr/bin/env sh\n")
      (.write writer "clojure -M:test \"$@\"\n"))
    (exec-sh-cmd "chmod +x bin/kaocha")))