(ns market-sentinel.stocks.core
  (:require [market-sentinel.stocks.fundamentals :refer [fetch-and-persist-tickers-fundamentals load-persisted-tickers-fundamentals]]
            [clojure.math.numeric-tower :refer [expt]]))

(def tickers ["AAPL" "GOOG" "NVDA"])

(def stock-params (let [pe-nasdaq-avg            25.03
                        pe-snp-500-avg           23.27
                        pe-safety-margin-percent 25]
                    {:nasdaq-avg-pe     pe-nasdaq-avg
                     :snp-500-avg-pe    pe-snp-500-avg
                     :pe-healthy-target (* pe-snp-500-avg (- 1 (/ pe-safety-margin-percent 100)))
                     :growth-1y-weight  2
                     :growth-5y-weight  5}))

(defn generate-ticker-analysis [fundamental-data]
  (let [{:keys [reference]}              fundamental-data
        {:keys [trailing-pe
                forward-pe]} reference
        ;; TODO: get it from the API
        growth-1y-percent                17.06
        growth-5y-percent                190.5
        growth-1y-expectation            (/ (+ (* (/ growth-1y-percent 100) (:growth-1y-weight stock-params))
                                               (*
                                                (expt (/ growth-5y-percent 100) (/ 1 5))
                                                (:growth-5y-weight stock-params)))
                                            (+ (:growth-1y-weight stock-params) (:growth-5y-weight stock-params)))]
    (into {}  [fundamental-data
               {:analysis {:growth-target-by-trailing-pe (- (+ 1 growth-1y-expectation) (/ trailing-pe (:pe-healthy-target stock-params)))
                           :growth-target-by-forward-pe  (-  (+ 1 growth-1y-expectation) (/ forward-pe (:pe-healthy-target stock-params)))
                           :growth-1y-expectation        growth-1y-expectation}}])))

(->>  (load-persisted-tickers-fundamentals)
      (map
       generate-ticker-analysis))

(comment
  (fetch-and-persist-tickers-fundamentals tickers)
  (load-persisted-tickers-fundamentals))
