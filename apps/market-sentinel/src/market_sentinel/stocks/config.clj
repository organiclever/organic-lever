(ns market-sentinel.stocks.config)

(defn calculate-healthy-pe
  "`calculate-healthy-pe` will calculate healthy PE for a given PE and safety margin percentage. It accept PE and safety margin percentage as input and return healthy PE as output. It will return `nil` when either PE or safety margin percentage is `nil`."
  [pe safety-margin-pct]
  (if (or (nil? pe) (nil? safety-margin-pct))
    nil
    (* pe (- 1 (/ safety-margin-pct 100)))))

(def stock-market-config
  "`stock-market-config` contains the configuration for the stock market. It can be used to predict the future of a given ticker."
  (let [pe-nasdaq-avg        25.03
        pe-snp-500-avg       23.27
        pe-safety-margin-pct 25]
    {:nasdaq-avg-pe     pe-nasdaq-avg
     :snp-500-avg-pe    pe-snp-500-avg
     :pe-healthy-target (calculate-healthy-pe pe-snp-500-avg pe-safety-margin-pct)
     :growth-1y-weight  2
     :growth-5y-weight  5}))