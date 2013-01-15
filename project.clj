(defproject mire "0.13.1"
  :description "A multiuser text adventure game/learning project."
  :main ^:skip-aot mire.server
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [server-socket "1.0.0"]
				  [clj-http "0.6.3"]
				[org.clojure/data.json "0.2.0"]])
