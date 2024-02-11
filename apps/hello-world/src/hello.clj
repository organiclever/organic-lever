(ns hello
  (:require [hello-lib.hello-time :as ht]))

(println "Hello world, the time is" (ht/time-str (ht/now)))
