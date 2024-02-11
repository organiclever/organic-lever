(ns market-sentinel.utils.col-extractor)

;; TODO: add-tests
(defn select-nested-keys [paths m]
  (into {} (map (fn [p]
                  [(last p) (get-in m p)]))
        paths))

(defn select-nested-keys-and-rename [opts m]
  (into {}
        (map (fn [[key path]]
               [key (get-in m path)]))
        opts))