(ns market-sentinel.stocks.fundamentals
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.io :as clojure.java.io]
            [clojure.string :as clojure.string]
            [market-sentinel.stocks.infra :refer [call-stocks-api]]
            [market-sentinel.stocks.utils :refer [load-ticker-data-from-edn
                                                  save-ticker-data-as-edn]]
            [market-sentinel.utils.col-extractor :refer [select-nested-keys-and-rename]]))

(def fetch-ticker-fundamentals
  (fn [ticker]
    (->
     (call-stocks-api
      client/get
      (str "fundamentals/" ticker))
     :body
     (json/read-str :key-fn keyword))))

(defn fetch-and-persist-tickers-fundamentals
  [tickers]
  (doseq [ticker tickers]
    (->> ticker
         fetch-ticker-fundamentals
         (save-ticker-data-as-edn ticker "fundamental"))))

(defn list-persisted-tickers
  []
  (->> (seq (.list (clojure.java.io/file "data/flat-files/stocks")))
       (filter (fn [x] (.contains x "fundamental")))
       (map (fn [x] (clojure.string/split x #"-")))
       (map first)
       (map clojure.string/upper-case)))

(:General (load-ticker-data-from-edn "AAPL" "fundamental"))

(defn load-ticker-fundamental
  [ticker]
  (let
   [data         (load-ticker-data-from-edn ticker "fundamental")
    cleaned-data (->>
                  data
                  (select-nested-keys-and-rename
                   [[:name [:General :Name]]
                    [:code [:General :Code]]
                    [:sector [:General :Sector]]
                    [:industry [:General :Industry]]
                    [:trailing-pe [:Valuation :TrailingPE]]
                    [:forward-pe [:Valuation :ForwardPE]]
                    [:profit-margin [:Highlights :ProfitMargin]]
                    [:dividend-yield [:Highlights :DividendYield]]
                    [:operating-margin-ttm [:Highlights :OperatingMarginTTM]]
                    [:market-capitalization [:Highlights :MarketCapitalization]]
                    [:wallstreet-target-price [:Highlights :WallStreetTargetPrice]]
                    [:analyst-rating [:AnalystRatings :Rating]]
                    [:analyst-target-price [:AnalystRatings :TargetPrice]]
                    [:analyst-strong-buy [:AnalystRatings :StrongBuy]]
                    [:analyst-buy [:AnalystRatings :Buy]]
                    [:analyst-hold [:AnalystRatings :Hold]]
                    [:analyst-sell [:AnalystRatings :Sell]]
                    [:analyst-strong-sell [:AnalystRatings :StrongSell]]
                    [:updated-at [:General :UpdatedAt]]]))]
    (into {:reference cleaned-data} [])))

(defn load-tickers-fundamentals [tickers]
  (let [fundamentals (map load-ticker-fundamental tickers)]
    (into [] fundamentals)))

(defn load-persisted-tickers-fundamentals
  []
  (load-tickers-fundamentals (list-persisted-tickers)))

(comment
  (fetch-and-persist-tickers-fundamentals ["AAPL" "GOOG"])
  (load-ticker-fundamental "AAPL")
  (load-tickers-fundamentals ["AAPL" "GOOG"]))