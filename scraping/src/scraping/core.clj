(ns scraping.core
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [dotenv :as env]
   [etaoin.api :as e]))

(def supported-url-format "https://recursionist.io/dashboard/problems/")

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

(defn extract-after-allow [s]
  (second (re-find #"--> (.*)"  s)))

(defn- extract-output-strings [s]
  (let [list-string (clojure.string/split-lines s)]
    (map extract-after-allow list-string)))

(defn extract-inside-parens [s]
  (second (re-find #"\((.*?)\)" s)))

(defn- extract-input-strings [s]
  (let [list-string (clojure.string/split-lines s)]
    (map extract-inside-parens list-string)))

(defn validate-url [url]
  (not= nil (re-matches #"https?://recursionist.io/dashboard/problems/.*" url)))

(defn validate-args [args]
  (or (some #(when (not (validate-url %)) %) args) true))

(defn split-into-list [data]
  (map #(if (str/includes? % ",")
          (str/split % #",")
          %)
       data))

(defn write-json-file [data filepath]
  (with-open [wtr (io/writer filepath)]
    (json/generate-stream data wtr {:pretty true})))

(defn parse-numbers [data]
  (map #(try
          (cond
            (re-matches #"\d+\.\d+" %) (Double/parseDouble %)
            (re-matches #"\d+" %) (Integer/parseInt %)
            :else %)
          (catch Exception _ %))
       data))


(defn get-testcase-value [driver url]
  ;; extract strings
  (let [testcase-string (get-testcase-string driver url)
        inputs (->  (extract-input-strings testcase-string)
                    split-into-list)
        outputs (-> (extract-output-strings testcase-string)
                    split-into-list)]
          ;; return map
    {:url url :inputs inputs :outputs outputs}))

(defn main-process [urls]
  (let [driver (e/chrome)]
    (try
      (login driver)

      (doall (map #(get-testcase-value driver %) urls))
      (catch Exception e
        (throw e))
      (finally (e/quit driver)))))

(defn args-empty []
  (do (println "引数として、少なくとも1つのURLを指定してください。")
      (println "RecursionのURL形式のみ対応しています。")
      (println "RecursionのURL形式:" supported-url-format)
      (println "使いかた: java -jar problem-value-scraping.jar https://recursionist.io/dashboard/problems/1")))

(def output-filepath "testcase.json")

(defn -main [& args]
  (let [validation-result (validate-args args)]
    (cond
      (empty? args) (args-empty)

      (not (true? validation-result))
      (do
        (println "RecursionのURL形式になっていません。")
        (println "エラー対象の引数:" validation-result)
        (println "対応している形式: " supported-url-format))

      :else (let [value-map (main-process args)]
              (write-json-file value-map output-filepath)))))

(defn read-json-file [filepath]
  (with-open [rdr (io/reader filepath)]
    (json/parse-stream rdr true)))

(read-json-file output-filepath)


; 引数なし
(-main)

;; 有効でないURLの引数
(-main "https://recursionist.io/")

(-main
 (str supported-url-format "5"))

;; 有効なURLの引数
(-main
 (str supported-url-format "1")
 (str supported-url-format "2")
 (str supported-url-format "3")
 (str supported-url-format "4")
 (str supported-url-format "5"))



(def data {:name "Alice"
           :age 17
           :hobbies ["reading" "coding"]})

(println (json/generate-string data {:pretty true}))

(def testcase-data {:url "https://recursionist.io/dashboard/problems/1" :inputs '([3 2] [2 10] [18 5] [8 14] [20 5]) :outputs '(1 -8 13 -6 15)})
(println (json/generate-string testcase-data {:pretty true}))

(write-json-file testcase-data "Recursion_testcase.json")



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

