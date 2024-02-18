(ns market-sentinel.stocks.utils
  (:require [clojure.edn :as edn]
            [market-sentinel.utils.pretty-spit :refer [pretty-spit-edn]]))

(def load-ticker-data-as-edn
  (fn [ticker suffix data]
    (pretty-spit-edn (str "data/flat-files/stocks/" ticker "-" suffix  ".edn") data)))

(defn extract-ticker-data-from-edn
  [ticker suffix]
  (-> (str "data/flat-files/stocks/" ticker "-" suffix ".edn")
      slurp
      edn/read-string))