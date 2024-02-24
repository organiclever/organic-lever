(ns market-sentinel.stocks.predictions
  (:require
   [market-sentinel.stocks.eod :refer [extract-eod-summary-for-ticker]]
   [market-sentinel.stocks.fundamentals :refer [extract-ticker-consensus extract-ticker-fundamentals]]))

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

(comment
  (extract-ticker-data-for-predictions
   {:code     "CELH"
    :exchange "NASDAQ"}))