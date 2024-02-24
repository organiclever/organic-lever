(ns market-sentinel.stocks.fundamentals
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [honey.sql :as sql]
            [market-sentinel.infra.db.core :refer [ds]]
            [market-sentinel.stocks.infra :refer [call-stocks-api]]
            [market-sentinel.utils.col-extractor :refer [select-nested-keys-and-rename]]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import [java.time LocalDate]))

(defn fetch-ticker-fundamentals
  "fetch-ticker-fundamentals will fetch the fundamentals of a given ticker"
  [ticker]
  (println (str "Fetching fundamentals data for " ticker))
  (->
   (call-stocks-api
    client/get
    (str "fundamentals/" ticker))
   :body
   (json/read-str :key-fn keyword)))

(defn clean-ticker-fundamental
  "clean-ticker-fundamental will extract cleaned fundamental data of a given ticker"
  [ticker-fundamental]
  (let
   [picked-data  (->>
                  ticker-fundamental
                  (select-nested-keys-and-rename
                   [[:name [:General :Name]]
                    [:code [:General :Code]]
                    [:description [:General :Description]]
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
                    [:updated-at [:General :UpdatedAt]]]))
    cleaned-data (merge
                  picked-data
                  {:dividend-yield (if (nil? (:dividend-yield picked-data)) 0 (:dividend-yield picked-data))})]
    (into  cleaned-data [])))

(defn store-tickers-fundamentals!
  "store-tickers-fundamentals will store the fundamentals of a given list of tickers"
  [tickers-fundamentals]

  (jdbc/with-transaction [tx ds]
    ;; save general info
    (jdbc/execute!
     tx (sql/format
         {:insert-into   :market-sentinel.stock_general_info
          :values        (map
                          (fn [{:keys [code name description sector industry]}]
                            {:stock_ticker_code code
                             :name              name
                             :description       description
                             :sector            sector
                             :industry          industry})
                          tickers-fundamentals)
          :on-conflict   [:stock_ticker_code]
          :do-update-set {:fields [:stock_ticker_code :name :description :sector :industry]}}))
    ;; save consensus history
    (jdbc/execute!
     tx
     (sql/format
      {:insert-into   :market-sentinel.stock_consensus_history
       :values        (map
                       (fn [{:keys [code updated-at wallstreet-target-price analyst-rating analyst-target-price analyst-strong-buy analyst-buy analyst-hold analyst-sell analyst-strong-sell]}]
                         {:stock_ticker_code       code
                          :date                    (LocalDate/parse updated-at)
                          :wallstreet_target_price wallstreet-target-price
                          :analyst_rating          analyst-rating
                          :analyst_target_price    analyst-target-price
                          :analyst_strong_buy      analyst-strong-buy
                          :analyst_buy             analyst-buy
                          :analyst_hold            analyst-hold
                          :analyst_sell            analyst-sell
                          :analyst_strong_sell     analyst-strong-sell})
                       tickers-fundamentals)
       :on-conflict   [:stock_ticker_code :date]
       :do-update-set {:fields [:stock_ticker_code :wallstreet_target_price :analyst_rating :analyst_target_price :analyst_strong_buy :analyst_buy :analyst_hold :analyst_sell :analyst_strong_sell]}}))
    ;; store fundamentals history
    (jdbc/execute!
     tx
     (sql/format
      {:insert-into   :market-sentinel.stock_fundamentals_history
       :values        (map
                       (fn [{:keys [code updated-at trailing-pe forward-pe profit-margin dividend-yield operating-margin-ttm market-capitalization]}]
                         {:stock_ticker_code     code
                          :date                  (LocalDate/parse updated-at)
                          :trailing_pe           trailing-pe
                          :forward_pe            forward-pe
                          :profit_margin         profit-margin
                          :dividend_yield        dividend-yield
                          :operating_margin_ttm  operating-margin-ttm
                          :market_capitalization market-capitalization})
                       tickers-fundamentals)
       :on-conflict   [:stock_ticker_code :date]
       :do-update-set {:fields [:stock_ticker_code :trailing_pe :forward_pe :profit_margin :dividend_yield :operating_margin_ttm :market_capitalization :updated_at]}}))))

(defn extract-all-tickers-fundamentals
  "extract-all-tickers-fundamentals will extract all tickers fundamentals from the database"
  []
  (jdbc/execute!
   ds
   (sql/format
    {:select [:stock_ticker_code
              :date
              :trailing_pe
              :forward_pe
              :profit_margin
              :dividend_yield
              :operating_margin_ttm
              :market_capitalization]
     :from   [[:market-sentinel.stock_fundamentals_history :sfh]]
     :where  [:= :sfh.date
              {:select [:%max.date]
               :from   [:market-sentinel.stock_fundamentals_history]
               :where  [:= :stock_ticker_code :sfh.stock_ticker_code]}]})
   {:builder-fn rs/as-unqualified-lower-maps}))

(defn extract-ticker-fundamentals "extract-ticker-fundamentals will extract all tickers fundamentals from the database"
  [stock_ticker_code]
  (->>
   (jdbc/execute!
    ds
    (sql/format
     {:select   [:stock_ticker_code
                 :date
                 :trailing_pe
                 :forward_pe
                 :profit_margin
                 :dividend-yield
                 :operating_margin_ttm
                 :market_capitalization]
      :from     [:market-sentinel.stock_fundamentals_history]
      :where    [:= :stock_ticker_code stock_ticker_code]
      :order-by [[:date :desc]]
      :limit    1})
    {:builder-fn rs/as-unqualified-lower-maps})
   first))

(defn extract-ticker-consensus
  "extract-ticker-consensus will extract all tickers consensus from the database"
  [stock_ticker_code]
  (->>
   (jdbc/execute!
    ds
    (sql/format
     {:select   [:stock_ticker_code
                 :wallstreet-target-price
                 :analyst-rating
                 :analyst-target-price
                 :analyst-strong-buy
                 :analyst-buy
                 :analyst-hold
                 :analyst-sell
                 :analyst-strong-sell
                 :date]
      :from     [:market-sentinel.stock_consensus_history]
      :where    [:= :stock_ticker_code stock_ticker_code]
      :order-by [[:date :desc]]
      :limit    1})
    {:builder-fn rs/as-unqualified-lower-maps})
   first))

(comment
  (extract-ticker-fundamentals "CELH")
  (extract-ticker-consensus "CELH"))