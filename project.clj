(defproject scraping "0.1.0-SNAPSHOT"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.1.230"]
                 [cheshire "5.13.0"]
                 [etaoin "1.1.42"]
                 [io.github.bonigarcia/webdrivermanager "5.9.2"]
                 [org.seleniumhq.selenium/selenium-java "4.10.0"]
                 [lynxeyes/dotenv "1.1.0"]
                 [clj-http/clj-http "3.13.0"]]
  :main scraping.core
  :aot :all
  :uberjar-name "Recursion-scraping.jar"
  :javac-options ["--release" "8"]
  :repl-options {:init-ns scraping.core})
