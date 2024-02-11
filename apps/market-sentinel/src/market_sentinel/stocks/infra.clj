(ns market-sentinel.stocks.infra
  (:require [market-sentinel.config :as config]
            [market-sentinel.infra.rest.api-client :refer [gen-call-rest]]))

(def call-stocks-api
  (gen-call-rest
   "https://eodhd.com/api/"
   {:headers      {:Content-type "application/json"}
    :query-params {:api_token (:api-key (:eodhd config/secrets))
                   :fmt       "json"}
    :debug        false}))