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
  (e/wait driver 2)
  (e/go driver url)
  (e/get-element-text driver {:css "#object-creator-div > div.py-3.my-0.mr-0 > div.p-3.testcaseBox"}))

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
  (not= nil (re-matches #"https?://recursionist.io/dashboard/problems/.*" url)))

(defn get-testcase-value [driver url]
  ;; extract strings
  (let [testcase-string (get-testcase-string driver url)
        inputs (extract-input-strings testcase-string)
        outputs (extract-output-strings testcase-string)]
          ;; return map
    {:inputs inputs :outputs outputs}))

(defn main-process [urls]
  (let [driver (e/chrome)]
    (try
      ;; login
      (login driver)

      (doall (map #(get-testcase-value driver %) urls))
      (catch Exception e
        (throw e))
      (finally (e/quit driver)))))

(def supported-url-format "https://recursionist.io/dashboard/problems/")

(defn process-url [url]
  (if-not (validate-url url)
    (do (println "RecursionのURL形式にのみ対応しています。")
        (println "対応している形式: " supported-url-format))
    (println "Processing URL:" url)))

(defn args-empty []
  (do (println "引数として、少なくとも1つのURLを指定してください。")
      (println "(RecursionのURL形式のみ対応しています。)")
      (println "(RecursionのURL：" supported-url-format ")")
      (println "使いかた: java -jar problem-value-scraping.jar https://recursionist.io/dashboard/problems/1")))

(defn -main [& args]
  (if (empty? args)
    (args-empty)
    (let [value-map (main-process args)]
      (println value-map))))

;; 引数なし
(-main)

;; 有効でないURLの引数
(-main "https://recursionist.io/")

;; 有効なURLの引数
(-main (str supported-url-format "1"))

;; 複数URLのテスト
(-main
 (str supported-url-format "1")
 (str supported-url-format "2")
 (str supported-url-format "3")
 (str supported-url-format "4")
 (str supported-url-format "5")
 (str supported-url-format "6")
 (str supported-url-format "7")
 (str supported-url-format "8"))


(-main supported-url-format)



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

