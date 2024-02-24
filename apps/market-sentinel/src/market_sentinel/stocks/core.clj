(ns market-sentinel.stocks.core
  (:require [market-sentinel.stocks.eod :refer [fetch-tickers-eods
                                                store-tickers-eods!]]
            [market-sentinel.stocks.fundamentals
             :refer [clean-ticker-fundamental fetch-ticker-fundamentals
                     store-tickers-fundamentals!]]
            [market-sentinel.stocks.predictions :refer [extract-ticker-data-for-predictions predict-ticker store-tickers-predictions!]]
            [market-sentinel.stocks.tickers :refer [extract-all-stock-tickers]]))

(comment
  ;; get fundamental data
  (->> (extract-all-stock-tickers)
       (map
        (fn [m] (->> (:code  m)
                     (fetch-ticker-fundamentals)
                     (clean-ticker-fundamental))))
       (store-tickers-fundamentals!))
  ;; get eod data
  (->> (extract-all-stock-tickers)
       (map (fn [m]  (:code  m)))
       (fetch-tickers-eods (* 365 5))
       (map (fn [[ticker eods]] (store-tickers-eods! {ticker eods}))))
  ;; generate prediction data
  (->> (extract-all-stock-tickers)
       (map (fn [ticker]
              (->> ticker
                   extract-ticker-data-for-predictions
                   predict-ticker)))
       (store-tickers-predictions!)))