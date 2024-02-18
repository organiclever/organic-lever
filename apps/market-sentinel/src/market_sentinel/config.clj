(ns market-sentinel.config
  (:require
   [clojure.edn :as edn]))

(def app_config (edn/read-string (slurp "app_config.edn")))

(def app_secrets (edn/read-string (slurp "app_secrets.edn")))

