# BRIER

This is an extension of Phil Hagelberg's Mire that can interact with the real world via HTTP requests. Done for API Hackday January 12, 2013

# Mire

It's a nonviolent MUD. (Multi-User Dungeon)

##Todos
-Tweak Ordrin's calls to take an input address. Address was hard coded in for the event to make the demo easier to run.
-Take the Menu output one step further to print children of each heading
-Add user interactions

## Usage

Install [Leiningen](http://leiningen.org) if you haven't already:

    $ curl -O ~/bin/lein http://github.com/technomancy/leiningen/raw/stable/bin/lein
    $ chmod 755 bin/lein

Then do `lein run` inside the Mire directory to launch the Mire
server. Then players can connect by telnetting to port 3333.

Copyright © 2009-2012 Phil Hagelberg
Licensed under the same terms as Clojure.
