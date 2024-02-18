(ns market-sentinel.stocks.eod
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [market-sentinel.stocks.infra :refer [call-stocks-api]]
            [market-sentinel.stocks.utils :refer [load-ticker-data-as-edn]])
  (:import [java.time LocalDate]))

(defn extract-ticker-eod
  [from to ticker]
  (->
   (call-stocks-api
    client/get
    (str "eod/" ticker)
    {:query-params {:from from
                    :to   to}})
   :body
   (json/read-str :key-fn keyword)))

(defn extract-and-store-tickers-eod
  [days-before tickers]
  (doseq [ticker tickers]
    (->> ticker
         (extract-ticker-eod
          (str (-> (LocalDate/now)
                   (.plusDays (* -1 days-before))))
          (str (LocalDate/now)))
         (load-ticker-data-as-edn ticker (str "eod-" days-before "d")))))

(defn extract-and-store-tickers-eod-bundle
  [tickers]
  (extract-and-store-tickers-eod (int (Math/floor (/ 365 2))) tickers)
  (extract-and-store-tickers-eod 365 tickers)
  (extract-and-store-tickers-eod (* 365 3) tickers)
  (extract-and-store-tickers-eod (* 365 5) tickers))

(sort-by :date
         #(compare %2 %1)
         (extract-ticker-eod
          (str (-> (LocalDate/now)
                   (.plusYears -5)))
          (str (LocalDate/now))
          "AAPL"))

(comment
  (extract-and-store-tickers-eod-bundle ["QCOM" "CELH" "NVDA" "TSM"]))