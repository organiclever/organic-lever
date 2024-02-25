(ns market-sentinel.stocks.fundamentals-test
  (:require [clojure.test :refer [deftest is testing]]
            [market-sentinel.stocks.fundamentals :as fundamentals]))

(deftest clean-ticker-fundamental-test
  (testing "clean-ticker-fundamental should return nil if the data is not available or error occurred."
    (let [ticker-fundamental nil
          cleaned-data       (fundamentals/clean-ticker-fundamental ticker-fundamental)]
      (is (nil? cleaned-data))))
  (testing "clean-ticker-fundamental should return the correct cleaned data."
    (let [ticker-fundamental {:General        {:Name        "Apple Inc."
                                               :Code        "AAPL"
                                               :Description "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide."
                                               :Sector      "Technology"
                                               :Industry    "Consumer Electronics"
                                               :UpdatedAt   "2021-08-01"}
                              :Valuation      {:TrailingPE 28.6
                                               :ForwardPE  23.5}
                              :Highlights     {:ProfitMargin          21.3
                                               :DividendYield         0.6
                                               :OperatingMarginTTM    24.3
                                               :MarketCapitalization  2.2E12
                                               :WallStreetTargetPrice 150.0}
                              :AnalystRatings {:Rating      "Buy"
                                               :TargetPrice 150.0
                                               :StrongBuy   15
                                               :Buy         20
                                               :Hold        30
                                               :Sell        10
                                               :StrongSell  5}}
          cleaned-data       (fundamentals/clean-ticker-fundamental ticker-fundamental)]
      (is (= {:name                    "Apple Inc."
              :code                    "AAPL"
              :description             "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide."
              :sector                  "Technology"
              :industry                "Consumer Electronics"
              :trailing-pe             28.6
              :forward-pe              23.5
              :profit-margin           21.3
              :dividend-yield          0.6
              :operating-margin-ttm    24.3
              :market-capitalization   2.2E12
              :wallstreet-target-price 150.0
              :analyst-rating          "Buy"
              :analyst-target-price    150.0
              :analyst-strong-buy      15
              :analyst-buy             20
              :analyst-hold            30
              :analyst-sell            10
              :analyst-strong-sell     5
              :updated-at              "2021-08-01"}
             cleaned-data)))))
;; => #'market-sentinel.stocks.fundamentals-test/clean-ticker-fundamental-test
