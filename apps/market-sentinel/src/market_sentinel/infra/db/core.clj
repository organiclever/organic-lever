(ns market-sentinel.infra.db.core
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]))

;; TODO: use env vars
(def db-spec {:dbtype   "postgres"
              :dbname   "postgres"
              :user     "market-sentinel"
              :password "market-sentinel-password"
              :port     5432})

(def ds (jdbc/get-datasource db-spec))

(defn init-db
  "init-db create all tables for market-sentinel. It accepts unused argument to be used with cli."
  [_]
  (jdbc/execute! ds [(slurp "resources/db/schema.sql")]))

(defn delete-db
  "delete-db drop all tables for market-sentinel. It accepts unused argument to be used with cli."
  [_]
  (jdbc/execute! ds ["DROP SCHEMA IF EXISTS \"sentinel\" CASCADE;"]))

(defn reset-db
  "reset-db delete and init db according to schema.sql. It accepts unused argument to be used with cli."
  [_]
  (delete-db nil)
  (init-db nil))

(comment
  (jdbc/execute! ds (sql/format {:select [:*]
                                 :from   [:sentinel.teams]}))
  (reset-db nil))
