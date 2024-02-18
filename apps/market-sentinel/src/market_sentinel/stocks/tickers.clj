(ns market-sentinel.stocks.tickers
  (:require [honey.sql :as sql]
            [market-sentinel.infra.db.core :refer [ds]]
            [next.jdbc :as jdbc]))

(defn load-stock-tickers!
  "load-stock-tickrers! stores tickers data in postgres database, it will use upsert strategies"
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
