(ns market-sentinel.stocks.eod
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [honey.sql :as sql]
            [market-sentinel.infra.db.core :refer [ds]]
            [market-sentinel.stocks.infra :refer [call-stocks-api]]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time]
            [next.jdbc.result-set :as rs])
  (:import [java.time LocalDate]))

(defn fetch-ticker-eods
  "fetch-ticker-eod will fetch the end of day data for a given ticker from the eodhd.com API. It will return the data as a Clojure map."
  ([from to ticker]
   (println (str "Fetching eod data for " ticker " from " from " to " to))
   (->
    (call-stocks-api
     client/get
     (str "eod/" ticker)
     {:query-params {:from from
                     :to   to}})
    :body
    (json/read-str :key-fn keyword)))
  ([days-before ticker]
   (fetch-ticker-eods
    (str (-> (LocalDate/now)
             (.plusDays (* -1 days-before))))
    (str (LocalDate/now))
    ticker)))

(defn fetch-tickers-eods
  "fetch-tickers-eod will fetch the end of day data for a given list of tickers from the eodhd.com API. It will return the data as a Clojure map."
  ([days-before tickers]
   (into {} (map (fn [ticker]
                   [ticker (fetch-ticker-eods days-before ticker)])
                 tickers)))
  ([from to tickers]
   (into {} (map (fn [ticker]
                   [ticker (fetch-ticker-eods from to ticker)])
                 tickers))))

(defn store-tickers-eods!
  "store-ticker-eod! will store the end of day data for a given ticker in the database."
  [tickers-eods]
  (let [merged-data (->> tickers-eods
                         (map (fn [[ticker eods]]
                                (map (fn [eod]
                                       (assoc eod :code ticker))
                                     eods)))
                         (flatten))]
    (jdbc/execute!
     ds (sql/format
         {:insert-into   :market-sentinel.stock_eods
          :values        (map
                          (fn [{:keys [date open high low close adjusted_close volume code]}]
                            {:stock_ticker_code code
                             :date              (LocalDate/parse date)
                             :open              open
                             :high              high
                             :low               low
                             :close             close
                             :adjusted_close    adjusted_close
                             :volume            volume})
                          merged-data)
          :on-conflict   [:stock_ticker_code :date]
          :do-update-set {:fields [:stock_ticker_code :date :open :high :low :close :adjusted_close :volume]}}))))

(defn extract-eod-summary-for-ticker
  "extract-eod-summary-for-ticker will extract the summary of the end of day data for a given ticker"
  [stock-ticker-code]
  (let
   [extract-eod (fn [minus-days]
                  (jdbc/execute!
                   ds
                   (sql/format
                    {:select   [:stock_ticker_code
                                :date
                                :open
                                :high
                                :low
                                :close
                                :adjusted_close
                                :volume]
                     :from     [:market-sentinel.stock_eods]
                     :where    [:and
                                [:= :stock_ticker_code stock-ticker-code]
                                [:>= :date (LocalDate/parse
                                            (str (-> (LocalDate/now)
                                                     (.plusDays (* -1 minus-days)))))]]
                     :order-by [[:date :desc]]})
                   {:builder-fn rs/as-unqualified-kebab-maps}))
    get-growth  (fn [eods]
                  ;; We use the adjusted_close to calculate the growth (including in the earliest-eod. This might result in accurate growth calculation, but it is good enough when we have long time horizonwe use the adjusted_close to calculate the growth, including in the earliest-eod. This might result inprecise calculations. But it is good enough when we have long time horizon.))
                  (let [earliest-eod (last eods)
                        latest-eod   (first eods)
                        growth-ratio (/
                                      (latest-eod :adjusted-close)
                                      (earliest-eod :adjusted-close))]
                    (* 100 (- growth-ratio 1))))
    eods-1y     (extract-eod 365)
    eods-5y     (extract-eod (* 365 5))
    growth-1y   (get-growth eods-1y)
    growth-5y   (get-growth eods-5y)]

    {:growth-1y-pct growth-1y
     :growth-5y-pct growth-5y}))

(comment
  (->> "CELH"
       extract-eod-summary-for-ticker)
  (->> (fetch-tickers-eods 5 ["AAPL" "MSFT"])
       store-tickers-eods!)
  (->> (fetch-ticker-eods
        10
        "AAPL")
       (sort-by
        :date
        #(compare %1 %2))
       (take 1)))