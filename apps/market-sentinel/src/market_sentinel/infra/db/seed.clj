(ns market-sentinel.infra.db.seed
  (:require [clojure.edn :as edn]
            [market-sentinel.stocks.tickers :refer [load-stock-tickers!]]))

(defn seed-tickers! []
  (println "Seeding tickers...")
  (let [stock_tickers (edn/read-string (slurp "data/seeds/stock_tickers.edn"))]
    (load-stock-tickers! stock_tickers))
  (println "Tickers seeded..."))

(defn seed!
  "seed! will fetch needed data to works with the database. Empty arg is needed to work with clojure cli"
  [_]
  ;; TODO: update this to use the new db functions
  (println "Seeding database...")
  (seed-tickers!)
  (println "Database seeded!"))

(comment (seed! nil))