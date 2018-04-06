(defproject dynamics-clj "0.1.1-SNAPSHOT"
  :description "Dynamics CRM Web API helper in Clojure"
  :url "https://github.com/d4hines/dynamics-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-codec "1.1.0"]
                 [clj-http "3.8.0"]
                 [cheshire "5.8.0"]
                 [environ "1.1.0"]
                 [slingshot "0.12.2"]]
  :plugins [[lein-environ "1.1.0"]])
