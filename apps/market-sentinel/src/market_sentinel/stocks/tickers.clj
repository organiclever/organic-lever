(ns market-sentinel.stocks.tickers
  (:require [clojure.edn :as edn]
            [honey.sql :as sql]
            [market-sentinel.infra.db.core :refer [ds]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn load-stock-tickers!
  "load-stock-tickrers! load tickers data in postgres database, it will use upsert strategies"
  [stock_tickers]
  (jdbc/execute!
   ds (sql/format
       {:insert-into   :market-sentinel.stock_tickers
        :values        (map
                        (fn [{:keys [code exchange]}]
                          {:code     code
                           :exchange exchange})
                        stock_tickers)
        :on-conflict   [:code]
        :do-update-set {:fields [:code :exchange]}})))

(defn extract-all-stock-tickers
  "extract-all-stock-tickers extract all stock tickers from the database"
  []
  (jdbc/execute!
   ds
   (sql/format {:select [:code :exchange]
                :from   [:market-sentinel.stock_tickers]})
   {:builder-fn rs/as-unqualified-lower-maps}))

(comment
  (load-stock-tickers!
   (edn/read-string (slurp "data/seeds/stock_tickers.edn")))
  (extract-all-stock-tickers))