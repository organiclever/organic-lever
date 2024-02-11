(ns market-sentinel.stocks.core
  (:require [market-sentinel.stocks.fundamentals :refer [fetch-and-persist-tickers-fundamentals load-persisted-tickers-fundamentals]]))

(def tickers ["AAPL" "GOOG" "NVDA"])

(comment
  (fetch-and-persist-tickers-fundamentals tickers)
  (load-persisted-tickers-fundamentals))
