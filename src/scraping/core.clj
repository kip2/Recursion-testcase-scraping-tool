(ns scraping.core
  (:gen-class)
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.tools.cli :as cli]
   [clojure.string :as str]
   [dotenv :as env]
   [etaoin.api :as e]))


;; url format
(def supported-url-format "https://recursionist.io/dashboard/problems/")

;; filepath
(def default-filepath "./testcase.json")
(def default-output-filename "testcase.json")

(defn- login [driver]
  (e/go driver "https://recursionist.io/")
  (e/set-window-size driver {:width 1280 :height 800})
  (e/wait-visible driver [{:class :front-page} {:id :topNavigation} {:data-target :#loginModal}])
  (e/click driver [{:class :front-page} {:id :topNavigation} {:data-target :#loginModal}])
  (e/wait driver 5)
  (e/wait-visible driver [{:tag :input :name :email}])
  (e/fill driver {:tag :input :name :email} (str (env/env :USER_EMAIL)))
  (e/wait-visible driver [{:tag :input :name :password}])
  (e/fill driver {:tag :input :name :password} (str (env/env :USER_PASSWORD)))
  (e/click driver [{:id :loginModal} {:tag :button :type :submit}]))

(defn- get-string [driver url]
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

(defn split-comma-into-list [data]
  (map #(if (str/includes? % ",")
          (str/split % #",")
          %)
       data))

(defn write-json-file [data filepath]
  (let [dir (io/file (.getParent (io/file filepath)))]
    (when (not (.exists dir))
      (.mkdirs dir))
    (with-open [wtr (io/writer filepath)]
      (json/generate-stream data wtr {:pretty true}))))


(defn parse-numbers [data]
  (map #(if (sequential? %)
          (parse-numbers %)
          (try
            (cond
              (re-matches #"[-+]?\d+\.\d+" %) (Double/parseDouble %)
              (re-matches #"[-+]?\d+" %) (Integer/parseInt %)
              :else %)
            (catch Exception _ %)))
       data))


(defn get-value [driver url]
  (let [string (get-string driver url)
        inputs (->  (extract-input-strings string)
                    split-comma-into-list
                    parse-numbers)
        outputs (-> (extract-output-strings string)
                    split-comma-into-list
                    parse-numbers)]
    (println "Success! Retrieved the following URL: " url)
    {:url url :inputs inputs :outputs outputs}))

(defn main-process [urls not-headless?]
  (let [driver (if not-headless?  (e/chrome) (e/chrome {:args ["--headless"]}))]
    (try
      (login driver)

      (doall (map #(get-value driver %) urls))
      (catch Exception e
        (throw e))
      (finally (e/quit driver)))))

(defn args-empty []
  (do (println "引数として、少なくとも1つのURLを指定してください。")
      (println "RecursionのURL形式のみ対応しています。")
      (println "RecursionのURL形式:" supported-url-format)
      (println "=== 使い方 ===")
      (println "java -jar Recursion-scraping.jar https://recursionist.io/dashboard/problems/1")))

(defn read-output-filepath []
  (env/env :OUTPUT_FILEPATH))

(defn create-output-filepath [env-path]
  (cond
    (str/blank? env-path) default-filepath
    (str/ends-with? env-path "/") (str env-path default-output-filename)
    :else env-path))

(def cli-options
  [["-h" "--help" "Show help."]
   ["-d" "--disabled-headless" "Disabled headless mode."]
   ["-f" "--file FILE" "Path to the input file."
    :validate [#(.exists (clojure.java.io/file %)) "File must exist."]]])

(defn parse-args [args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-options)]
    {:options options
     :arguments arguments
     :summary summary
     :errors errors}))

(defn print-help [parsed-args]
  (do
    (println (get-in parsed-args [:summary]))
    (println "=== 使いかた ===")
    (println "java -jar Recursion-scraping.jar https://recursionist.io/dashboard/problems/1")
    (println "=== ファイルパス指定の場合(UTF-8のファイルのみ対応) ===")
    (println "java -jar Recursion-scraping.jar input-file.txt")))

(defn slurp-file [filepath]
  (with-open [rdr (io/reader filepath :encoding "UTF-8")]
    (vec (line-seq rdr))))

(defn print-validation-error [args]
  (do
    (println "RecursionのURL形式になっていません。")
    (println "エラー対象の引数:" args)
    (println "対応している形式: " supported-url-format)))

(defn -main [& args]
  (let [parsed-args (parse-args args)
        help? (get-in parsed-args [:options :help])
        file? (some? (get-in parsed-args [:options :file]))
        args (get-in parsed-args [:arguments])
        not-headless? (get-in parsed-args [:options :disabled-headless])]
    (cond
      help? (print-help  parsed-args)

      :else (let [validation-error (validate-args args)
                  output-filepath (create-output-filepath (read-output-filepath))]
              (cond
                (and (false? file?) (empty? args)) (args-empty)

                (not (true? validation-error)) (print-validation-error validation-error)

                :else (let [value-map (cond
                                        file? (let  [input-filepath (get-in parsed-args [:options :file])
                                                     args (slurp-file input-filepath)]
                                                (main-process args not-headless?))
                                        :else (main-process args not-headless?))]
                        (write-json-file value-map output-filepath)))))))

;; (-main "-d" "https://recursionist.io/dashboard/problems/1")


