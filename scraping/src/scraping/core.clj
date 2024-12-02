(ns scraping.core
  (:gen-class)
  (:require [clojure.string :as str]
            [dotenv :as env]
            [etaoin.api :as e]))

(defn- login [driver]
  (e/go driver "https://recursionist.io/")
  (e/set-window-size driver {:width 1280 :height 800})
  (e/wait-visible driver [{:class :front-page} {:id :topNavigation} {:data-target :#loginModal}])
  (e/click driver [{:class :front-page} {:id :topNavigation} {:data-target :#loginModal}])
  (e/wait driver 5)
  (e/wait-visible driver [{:tag :input :name :email}])
  (e/fill driver {:tag :input :name :email} (str (env/env :USER_NAME)))
  (e/wait-visible driver [{:tag :input :name :password}])
  (e/fill driver {:tag :input :name :password} (str (env/env :PASSWORD)))
  (e/click driver [{:id :loginModal} {:tag :button :type :submit}]))

(defn- get-testcase-string [driver url]
  (e/go driver url)
  (e/get-element-text driver {:xpath "//*[@id='object-creator-div']/div[1]/div[2]"}))

(defn- extract-after-allow [s]
  (second (re-find #"--> (.*)"  s)))

(defn- extract-output-strings [s]
  (let [list-string (clojure.string/split-lines s)]
    (map extract-after-allow list-string)))

(defn- extract-inside-parens [s]
  (second (re-find #"\((.*?)\)" s)))

(defn- extract-input-strings [s]
  (let [list-string (clojure.string/split-lines s)]
    (map extract-inside-parens list-string)))

(defn- validate-url [url]
  (not= nil (re-matches #"https?://recursionist.io/.+" url)))

(defn get-input-output [url]
  (let [driver (e/chrome)]
    (try
      ;; validation of url
      (when-not (validate-url url)
        (throw (ex-info "Invalid URL" {:url url})))

      ;; login
      (login driver)

      ;; extract strings
      (let [testcase-string (get-testcase-string driver url)
            inputs (extract-input-strings testcase-string)
            outputs (extract-output-strings testcase-string)]
          ;; return map
        {:inputs inputs :outputs outputs})
      (catch Exception e
        (throw e))
      (finally (e/quit driver)))))

;; ~~~~~~~~~~~~~~
;; usage
;; ~~~~~~~~~~~~~~

;; prepare url
;; (def url "https://recursionist.io/dashboard/problems/8")

;; execute function & get return map
;; (def expected (get-input-output url))

;; confirmation
;; (println expected)
; {:inputs ("hello" "Good morning" "1234"), :outputs (o g 4)}

