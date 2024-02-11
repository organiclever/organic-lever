(ns market-sentinel.utils.pagination)

(defn get-more?
  "get-more? tests if we need to get more data from or not. It will assume that the index starts from 0."
  [start-at max-results total]
  (not (>= (+ start-at max-results) total)))

(defn fetch-paginated-data
  "fetch-paginated-data will fetch all data from the API. It accept fetch-items function as parameter, which will be called to fetch data from the API. The fetch-items function should accept starting index and maximum result, and return a map with the following keys: :startAt, :total, :maxResults, :items."
  [fetch-items]
  (let [acc (atom [])]
    (loop [res         (fetch-items 0 100)
           start-at    (:startAt res)
           max-results (:maxResults res)
           total       (:total res)]
      (swap! acc into (:items res))
      (when (get-more? start-at max-results total)
        (recur (fetch-items (+ start-at max-results) max-results)
               (+ start-at max-results)
               max-results
               total)))
    @acc))
