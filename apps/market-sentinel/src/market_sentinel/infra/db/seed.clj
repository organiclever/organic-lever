(ns market-sentinel.infra.db.seed)

(defn seed!
  "seed! will fetch needed data to works with the database. Empty arg is needed to work with clojure cli"
  [_]
  ;; TODO: update this to use the new db functions
  ;; (println "Seeding database...")
  ;; (println "Database seeded!")
  )

(comment (seed! nil))