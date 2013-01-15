(ns mire.commands
  (:use [mire.rooms :only [rooms room-contains?]]
        [mire.player]
		[mire.http])
  (:use [clojure.string :only [join]])
  (:require [clojure.data.json :as json])
)

(defn- move-between-refs
  "Move one instance of obj between from and to. Must call in a transaction."
  [obj from to]
  (alter from disj obj)
  (alter to conj obj))

;; Command functions

(defn look
  "Get a description of the surrounding environs and its contents."
  []
  (str (:desc @*current-room*)
       "\nExits: " (keys @(:exits @*current-room*)) 
		"\nPlayers here too: " (deref (:inhabitants @*current-room*)) "\n"
       (join "\n" (map #(str "There is " % " here.\n")
                           @(:items @*current-room*)))))

(defn move
  "\"♬ We gotta get out of this place... ♪\" Give a direction."
  [direction]
  (dosync
   (let [target-name ((:exits @*current-room*) (keyword direction))
         target (@rooms target-name)]
     (if target
       (do
         (move-between-refs *player-name*
                            (:inhabitants @*current-room*)
                            (:inhabitants target))
         (ref-set *current-room* target)
         (look))
       "You can't go that way."))))

(defn grab
  "Pick something up."
  [thing]
  (dosync
   (if (room-contains? @*current-room* thing)
     (do (move-between-refs (keyword thing)
                            (:items @*current-room*)
                            *inventory*)
         (str "You picked up the " thing "."))
     (str "There isn't any " thing " here."))))

(defn discard
  "Put something down that you're carrying."
  [thing]
  (dosync
   (if (carrying? thing)
     (do (move-between-refs (keyword thing)
                            *inventory*
                            (:items @*current-room*))
         (str "You dropped the " thing "."))
     (str "You're not carrying a " thing "."))))

(defn inventory
  "See what you've got."
  []
  (str "You are carrying:\n"
       (join "\n" (seq @*inventory*))))

(defn detect
  "If you have the detector, you can see which room an item is in."
  [item]
  (if (@*inventory* :detector)
    (if-let [room (first (filter #((:items %) (keyword item))
                                 (vals @rooms)))]
      (str item " is in " (:name room))
      (str item " is not in any room."))
    "You need to be carrying the detector for that."))

(defn say
  "Say something out loud so everyone in the room can hear."
  [& words]
  (let [message (join " " words)]
    (doseq [inhabitant (disj @(:inhabitants @*current-room*) *player-name*)]
      (binding [*out* (player-streams inhabitant)]
        (println (str *player-name* ": " message))
        (println prompt)))
    (str "You said " message)))

(defn sports
  "Look for sports news if you have a newspaper handy. Syntax example hockey/nhl"
	[sport]
	(if (carrying? :newspaper)
		(do
		(println "\nYou open up the newspaper and turn immediately to the sports section. Hmmm... what's going on in your favorite league right now?\n")
		(let [data (espn sport)]
			(doseq [item (let [bodydata (json/read-str (get-in data [:body]))]
				(get-in bodydata["headlines"]))
				]
				(println (get-in item["headline"]))))
		)
	(str "You can't do this unless you have the newspaper"))
)

(defn sms
	"Send sms to someone if you have a cellphone. Syntax +15555555 This is my message"
	[& msg]
	(if (carrying? :phone)
		(do
		(println "You whip out your brand new Galaxy Nexus... Man look at that resolution!")
		(println "Dialing...\n")
		(twilio msg)
		)
	(str "You can't do this until you have the phone")
	)
)

(defn order
	"Look up the restaurants in your area that deliver with your Zagats"
	[]
	(if (carrying? :zagats)
	(do
		(println "Mmm.. all this game play makes you hungry. You open your Zagats to see who delivers in your area...\n")
		(let [data (ordrin)]
			(doseq [item (json/read-str (get-in data [:body]))]
			(println (str (get-in item["id"]) " " (get-in item["na"]) " " (get-in item["cs_phone"])))
			)
		)
	)
		(str "You don't have a Zagats")
	)
)

(defn menu
	"Get restaurant menu by id listed in Zagats."
	[restid]
	(if (carrying? :zagats)
	(do
		(println "Pull menu data for the restaurant...\n")
		(let [data (menudata restid)]
			(let [menus (json/read-str (get-in data [:body]))]
			 (doseq [item (get-in menus["menu"])]
				(println (str (get-in item["name"])))
			)
		)
	)
	)
	(str "You don't have a Zagats")
	)
)

(defn help
  "Show available commands and what they do."
  []
  (join "\n" (map #(str (key %) ": " (:doc (meta (val %))))
                      (dissoc (ns-publics 'mire.commands)
                              'execute 'commands))))

;; Command data

(def commands {"move" move,
               "north" (fn [] (move :north)),
               "south" (fn [] (move :south)),
               "east" (fn [] (move :east)),
               "west" (fn [] (move :west)),
               "grab" grab
               "discard" discard
               "inventory" inventory
               "detect" detect
               "look" look
               "say" say
			   "sports" sports,
			   "sms" sms,
			   "order" order,
			   "menu" menu,
               "help" help})

;; Command handling

(defn execute
  "Execute a command that is passed to us."
  [input]
  (try (let [[command & args] (.split input " +")]
         (apply (commands command) args))
       (catch Exception e
         (.printStackTrace e (new java.io.PrintWriter *err*))
         "You can't do that!")))
