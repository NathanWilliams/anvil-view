(defproject anvil-view "0.1.0-SNAPSHOT"
  :description "Visualising Minecraft Anvil / Region files"
  :url "https://github.com/NathanWilliams/anvil-view"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[clj-ns-browser "1.3.2-SNAPSHOT-LOCAL"]
                                  [org.clojure/tools.namespace "0.2.2"]]}}

  :dependencies [[org.clojure/clojure "1.5.0"]
                 [anvil-clj "0.1.0-SNAPSHOT"]
                 [seesaw "1.4.3"]]
  :main anvil-view.devel)
