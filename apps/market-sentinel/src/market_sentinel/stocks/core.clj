(ns market-sentinel.stocks.core
  (:require [clojure.math.numeric-tower :refer [expt]]
            [market-sentinel.stocks.eod :refer [extract-and-store-tickers-eod-bundle]]
            [market-sentinel.stocks.fundamentals :refer [fetch-and-store-tickers-fundamentals
                                                         load-persisted-tickers-fundamentals]]
            [market-sentinel.utils.col-extractor :refer [select-nested-keys]]))

(def tickers ["NVDA" "QCOM" "TSM" "CELH"])

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
    (into {}
          [fundamental-data
           {:analysis {:growth-target-by-trailing-pe (-
                                                      (+ 1 growth-1y-expectation-percent)
                                                      (/ trailing-pe (:pe-healthy-target stock-params)))
                       :growth-target-by-forward-pe  (-
                                                      (+ 1 growth-1y-expectation-percent)
                                                      (/ forward-pe (:pe-healthy-target stock-params)))
                       :growth-1y-expectation        growth-1y-expectation-percent}}])))

(comment
  (fetch-and-store-tickers-fundamentals tickers)
  (extract-and-store-tickers-eod-bundle tickers)
  (load-persisted-tickers-fundamentals)
  (->>  (load-persisted-tickers-fundamentals)
        (map
         analyze-ticker)
        (map (fn [m] (select-nested-keys [[:reference :code]
                                          [:analysis :growth-target-by-trailing-pe]
                                          [:analysis :growth-target-by-forward-pe]
                                          [:analysis :growth-1y-expectation]] m)))))
