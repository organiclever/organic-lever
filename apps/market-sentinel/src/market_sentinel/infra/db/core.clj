(ns market-sentinel.infra.db.core
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [market-sentinel.config :refer [app_secrets app_config]]))

;; TODO: use env vars
(def db-spec {:dbtype   (get-in app_config [:db :dbtype])
              :dbname   (get-in app_config [:db :dbname])
              :host     (get-in app_secrets [:db :host])
              :user     (get-in app_secrets [:db :user])
              :password (get-in app_secrets [:db :password])
              :port     (get-in app_secrets [:db :port])})

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
  (delete-db nil)
  (init-db nil)
  (reset-db nil))
