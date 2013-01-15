(ns mire.http
  (:require [clj-http.client :as client])
  (:use [clojure.string :only [join]]
		[config.keys]))

(defn twilio
	"Making a SMS request to Twilio"
	[num]
	(do
	(println (first num))
	(let [tonum (first num) txtmsg (join " " (rest num))]
	(if (= 201 (get-in (client/post "https://api.twilio.com/2010-04-01/Accounts/ACc69b18846ee441fd84011ac02c390a70/SMS/Messages"
	{:basic-auth [twilio-sid twilio-token]
	   :form-params {:Body txtmsg
	   :To tonum
	   :From "+15169861375"}}
	)[:status]
		) )
		(str "Your text message has been sent")
		(str "Something went wrong"))
	))
)

(defn espn
	"Grabs data from ESPN's headlines API based on sport and league"
	[sport]
	(client/get (str "http://api.espn.com/v1/sports/" sport "/news/headlines/top?apikey=" espn-key)))
	
(defn ordrin
	"Grabs data from ordrin's restaurant api (no API key required)"
	[]
	(client/get "https://r-test.ordr.in/dl/ASAP/10018/7th%20Ave/500"))
	
(defn menudata
	"Grabs menu data for a specific restaurant"
	[rest]
	(client/get (str "https://r-test.ordr.in/rd/" rest)))