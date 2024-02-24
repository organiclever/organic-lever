(ns market-sentinel.stocks.predictions
  (:require [honey.sql :as sql]
            [market-sentinel.infra.db.core :refer [ds]]
            [market-sentinel.stocks.eod :refer [extract-eod-summary-for-ticker]]
            [market-sentinel.stocks.fundamentals :refer [extract-ticker-consensus
                                                         extract-ticker-fundamentals]]
            [market-sentinel.stocks.tickers :refer [extract-all-stock-tickers]]
            [next.jdbc :as jdbc]))

(defn extract-ticker-data-for-predictions
  "extract-ticker-data-for-highlights will extract necessary data for ticker highlights"
  [ticker]

  (println (str "Extracting fundamentals data for " (:code ticker) " of " (:exchange ticker)))

  (let [fundamentals (extract-ticker-fundamentals (:code ticker))
        consensus    (extract-ticker-consensus (:code ticker))
        growth       (extract-eod-summary-for-ticker (:code ticker))]

    {:ticker       ticker
     :fundamentals fundamentals
     :consensus    consensus
     :growth       growth}))

(def stock-params
  (let [pe-nasdaq-avg        25.03
        pe-snp-500-avg       23.27
        pe-safety-margin-pct 25]
    {:nasdaq-avg-pe     pe-nasdaq-avg
     :snp-500-avg-pe    pe-snp-500-avg
     :pe-healthy-target (*
                         pe-snp-500-avg
                         (- 1 (/ pe-safety-margin-pct 100)))
     :growth-6mo-weight 1
     :growth-1y-weight  2
     :growth-3y-weight  3
     :growth-5y-weight  5}))

(defn get-1y-growth-expectation-pct [growth-1y-pct  growth-5y-pct]
  (let [growth-1y-weight (:growth-1y-weight stock-params)
        growth-5y-weight (:growth-5y-weight stock-params)
        term-1y          (* (+ 1 (/ growth-1y-pct 100)) growth-1y-weight)
        avg-growth-5y    (Math/pow (+ 1 (/ growth-5y-pct 100)) (/ 1 5))
        term-5y          (* avg-growth-5y growth-5y-weight)
        numerator        (+ term-1y term-5y)
        denominator      (+ growth-1y-weight growth-5y-weight)]
    (* 100 (- (/ numerator denominator) 1))))

(defn predict-ticker
  "predict-ticker will predict the future of a given ticker"
  [ticker-info]
  (let
   [trailing-pe               (get-in ticker-info [:fundamentals :trailing-pe])
    forward-pe                (get-in ticker-info [:fundamentals :forward-pe])
    growth-1y-expectation-pct (get-1y-growth-expectation-pct
                               (:growth-1y-pct (:growth ticker-info))
                               (:growth-5y-pct (:growth ticker-info)))
    pe-healthy-target         (:pe-healthy-target stock-params)]

    {:stock-ticker-code                (:code (:ticker ticker-info))
     :profit_prediction-by-trailing-pe (try
                                         (- (*
                                             (+ 100 growth-1y-expectation-pct)
                                             (/ pe-healthy-target trailing-pe))
                                            100)
                                         (catch Exception _e nil))
     :profit_prediction-by-forward-pe  (try
                                         (- (*
                                             (+ 100 growth-1y-expectation-pct)
                                             (/ pe-healthy-target forward-pe))
                                            100)
                                         (catch Exception _e nil))
     :growth-1y-expectation            growth-1y-expectation-pct}))

(defn store-tickers-predictions!
  "store-tickers-predictions! will store the predictions of the tickers in the database"
  [ticker-predictions]

  (jdbc/execute!
   ds
   (sql/format
    {:insert-into   :market-sentinel.stock_predictions
     :values        (map
                     (fn [{:keys [stock-ticker-code profit_prediction-by-trailing-pe profit_prediction-by-forward-pe growth-1y-expectation]}]
                       {:stock_ticker_code                stock-ticker-code
                        :profit_prediction_by_trailing_pe profit_prediction-by-trailing-pe
                        :profit_prediction_by_forward_pe  profit_prediction-by-forward-pe
                        :growth_1y_prediction             growth-1y-expectation})
                     ticker-predictions)
     :on-conflict   [:stock_ticker_code]
     :do-update-set {:fields [:stock_ticker_code :profit_prediction_by_trailing_pe :profit_prediction_by_forward_pe :growth_1y_prediction :updated_at]}})))

(comment
  (->> {:code     "NVDA"
        :exchange "NASDAQ"}
       extract-ticker-data-for-predictions
       predict-ticker)
  (->> (extract-all-stock-tickers)
       (map (fn [ticker]
              (->> ticker
                   extract-ticker-data-for-predictions
                   predict-ticker)))
       (store-tickers-predictions!)))
