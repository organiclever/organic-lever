(ns market-sentinel.infra.rest.api-client)

(defmacro gen-call-rest
  "use this macro to create 'call-rest' function"
  [base-url base-config & _args]
  `(defn call-rest#
     ([method# res-url#]
      (method# (str ~base-url "/"  res-url#) ~base-config))
     ([method# res-url# additional-config#]
      (method# (str ~base-url "/"  res-url#)
               (merge ~base-config additional-config#)))))
