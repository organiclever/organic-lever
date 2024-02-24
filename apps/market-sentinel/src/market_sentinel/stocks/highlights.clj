(ns market-sentinel.stocks.highlights
  (:require [market-sentinel.stocks.predictions :refer [extract-ticker-data-for-predictions]]
            [market-sentinel.stocks.tickers :refer [extract-all-stock-tickers]]))

(comment
  (->> (extract-all-stock-tickers)
       (map extract-ticker-data-for-predictions)
       (take 1)))