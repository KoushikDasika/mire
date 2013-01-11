(ns mire.http
  (:require [clj-http.client :as client]))

(defn testthis
	[]
	(client/get "http://google.com"))