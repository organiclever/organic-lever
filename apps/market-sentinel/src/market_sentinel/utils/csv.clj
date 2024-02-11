(ns market-sentinel.utils.csv
  (:require [clojure.string :as string]))

;; TODO: create test
(defn csv-data->maps
  "csv-data->maps converts a csv data to a vector of maps, the first row is the header, and the keys are converted to keywords with lower case and spaces replaced by dashes"
  [csv-data]
  (map zipmap
       (->> (first csv-data)
            (map string/lower-case)
            (map #(string/replace % #" " "-"))
            (map keyword)
            repeat)
       (rest csv-data)))