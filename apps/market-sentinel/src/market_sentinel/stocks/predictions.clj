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
     :growth-1y-weight  2
     :growth-5y-weight  5}))

(defn predict-1y-growth-pct [growth-1y-pct  growth-5y-pct]
  (let [growth-1y-weight  (:growth-1y-weight stock-params)
        growth-5y-weight  (:growth-5y-weight stock-params)
        growth-1y-contrib (* (+ 1 (/ growth-1y-pct 100)) growth-1y-weight)
        avg-growth-5y     (Math/pow (+ 1 (/ growth-5y-pct 100)) (/ 1 5))
        growth-5y-contrib (* avg-growth-5y growth-5y-weight)
        numerator         (+ growth-1y-contrib growth-5y-contrib)
        denominator       (+ growth-1y-weight growth-5y-weight)]
    (try (* 100 (- (/ numerator denominator) 1))
         (catch Exception _e nil))))

(defn predict-ticker
  "predict-ticker will predict the future of a given ticker"
  [ticker-info]
  (let
   [trailing-pe              (get-in ticker-info [:fundamentals :trailing-pe])
    forward-pe               (get-in ticker-info [:fundamentals :forward-pe])
    pe-healthy-target        (:pe-healthy-target stock-params)
    growth-1y-prediction-pct (predict-1y-growth-pct
                              (:growth-1y-pct (:growth ticker-info))
                              (:growth-5y-pct (:growth ticker-info)))
    predict-profit           (fn [pe]
                               (try
                                 (- (*
                                     (+ 100 growth-1y-prediction-pct)
                                     (/ pe-healthy-target pe))
                                    100)
                                 (catch Exception _e nil)))]

    {:stock-ticker-code                (:code (:ticker ticker-info))
     :profit-prediction-by-trailing-pe (predict-profit trailing-pe)
     :profit-prediction-by-forward-pe  (predict-profit forward-pe)
     :growth-1y-prediction-pct         growth-1y-prediction-pct}))

(defn store-tickers-predictions!
  "store-tickers-predictions! will store the predictions of the tickers in the database"
  [ticker-predictions]

  (jdbc/execute!
   ds
   (sql/format
    {:insert-into   :market-sentinel.stock_predictions
     :values        (map
                     (fn [{:keys [stock-ticker-code profit-prediction-by-trailing-pe profit-prediction-by-forward-pe growth-1y-prediction-pct]}]
                       {:stock_ticker_code                stock-ticker-code
                        :profit_prediction_by_trailing_pe profit-prediction-by-trailing-pe
                        :profit_prediction_by_forward_pe  profit-prediction-by-forward-pe
                        :growth_1y_prediction             growth-1y-prediction-pct})
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
