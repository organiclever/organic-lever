(ns market-sentinel.config
  (:require
   [clojure.edn :as edn]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def app_config (edn/read-string (slurp "app_config.edn"))) ;; it is not used yet

(def app_secrets (edn/read-string (slurp "app_secrets.edn")))

