(ns market-sentinel.utils.pretty-spit
  (:require [clojure.data.json :as json]
            [clojure.pprint :as pp]))

(defn pretty-spit-edn
  [file-name src]
  (spit (java.io.File. file-name)
        (with-out-str (pp/write src :dispatch pp/code-dispatch))))

(defn pretty-spit-json
  [file-name src]
  (spit file-name (with-out-str (json/pprint src))))
