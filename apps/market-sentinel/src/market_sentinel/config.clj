(ns market-sentinel.config
  (:require
   [clojure.edn :as edn]))

(def config (edn/read-string (slurp "config/config.edn")))
(def secrets (edn/read-string (slurp "config/secrets.edn")))

(def github-org (get-in config [:github :organization-name]))