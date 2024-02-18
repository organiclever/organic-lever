(ns market-sentinel.stocks.core
  (:require [clojure.math.numeric-tower :refer [expt]]
            [market-sentinel.stocks.eod :refer [fetch-tickers-eods
                                                store-tickers-eods!]]
            [market-sentinel.stocks.fundamentals
             :refer [fetch-ticker-fundamentals get-ticker-fundamental
                     store-tickers-fundamentals!]]
            [market-sentinel.stocks.tickers :refer [extract-all-stock-tickers]]))

(def stock-params (let [pe-nasdaq-avg            25.03
                        pe-snp-500-avg           23.27
                        pe-safety-margin-percent 25]
                    {:nasdaq-avg-pe     pe-nasdaq-avg
                     :snp-500-avg-pe    pe-snp-500-avg
                     :pe-healthy-target (* pe-snp-500-avg (- 1 (/ pe-safety-margin-percent 100)))
                     :growth-6mo-weight 1
                     :growth-1y-weight  2
                     :growth-3y-weight  3
                     :growth-5y-weight  4}))

(defn get-1y-growth-expectation [growth-1y-percent growth-5y-percent]
  (/ (+ (* (/ growth-1y-percent 100) (:growth-1y-weight stock-params))
        (*
         (expt (/ growth-5y-percent 100) (/ 1 5))
         (:growth-5y-weight stock-params)))
     (+ (:growth-1y-weight stock-params) (:growth-5y-weight stock-params))))

(defn analyze-ticker [fundamental-data]
  (let [{:keys [reference]}              fundamental-data
        {:keys [trailing-pe
                forward-pe]} reference
        ;; TODO: get it from the API
        growth-1y-percent                17.06
        growth-5y-percent                190.5
        growth-1y-expectation-percent    (get-1y-growth-expectation growth-1y-percent growth-5y-percent)]
    (into
     {}
     [fundamental-data
      {:analysis {:growth-target-by-trailing-pe (-
                                                 (+ 1 growth-1y-expectation-percent)
                                                 (/ trailing-pe (:pe-healthy-target stock-params)))
                  :growth-target-by-forward-pe  (-
                                                 (+ 1 growth-1y-expectation-percent)
                                                 (/ forward-pe (:pe-healthy-target stock-params)))
                  :growth-1y-expectation        growth-1y-expectation-percent}}])))

(comment
  ;; get fundamental data
  (->> (extract-all-stock-tickers)
       (map
        (fn [m] (->> (:code  m)
                     (fetch-ticker-fundamentals)
                     (get-ticker-fundamental))))
       (store-tickers-fundamentals!))
  ;; get eod data
  (->> (extract-all-stock-tickers)
       (map (fn [m]  (:code  m)))
       (fetch-tickers-eods (* 365 5))
       (map (fn [[ticker eods]] (store-tickers-eods! {ticker eods})))))
