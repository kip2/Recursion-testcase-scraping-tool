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
  "Recursionへのログインを行う関数"
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
  "Recursionの問題ページから、入出力データを取得する関数"
  (e/wait driver 2)
  (e/go driver url)
  (e/get-element-text driver {:css "#object-creator-div > div.py-3.my-0.mr-0 > div.p-3.testcaseBox"}))

(defn extract-after-allow [s]
  "--> ()の形式の文字列から、()の中身を取得する関数"
  (second (re-find #"--> (.*)"  s)))

(defn- extract-output-strings [s]
  "取得対象であるテストケースの出力の値を切り出す関数"
  (let [list-string (clojure.string/split-lines s)]
    (map extract-after-allow list-string)))

(defn extract-inside-parens [s]
  "カッコの中の文字列を切り出す関数"
  (second (re-find #"\((.*?)\)" s)))

(defn- extract-input-strings [s]
  "取得対象であるテストケースの入力の値を切り出す関数"
  (let [list-string (clojure.string/split-lines s)]
    (map extract-inside-parens list-string)))

(defn validate-url [url]
  "urlがRecursionの問題ページの形式になっているかを判定する関数"
  (not= nil (re-matches #"https?://recursionist.io/dashboard/problems/.*" url)))

(defn validate-args [args]
  "引数のURL形式が正しいか判定し、正しくないものが含まれていた場合はそのURLを返す関数"
  (or (some #(when (not (validate-url %)) %) args) true))

(defn split-comma-into-list [data]
  "カンマ区切りの文字列をリストに切り出す関数"
  (map #(if (str/includes? % ",")
          (str/split % #",")
          %)
       data))

(defn write-json-file [data filepath]
  "dataをjsonに出力する関数"
  (let [dir (io/file (.getParent (io/file filepath)))]
    (when (not (.exists dir))
      (.mkdirs dir))
    (with-open [wtr (io/writer filepath)]
      (json/generate-stream data wtr {:pretty true}))))


(defn parse-numbers [data]
  "シーケンスの中の数字のみをintやfloatに変換する関数"
  (map #(if (sequential? %)
          (parse-numbers %)
          (try
            (cond
              (re-matches #"[-+]?\d+\.\d+" %) (Double/parseDouble %)
              (re-matches #"[-+]?\d+" %) (Integer/parseInt %)
              :else %)
            (catch Exception _ %)))
       data))

(defn numeric? [s]
  "文字列が数字によって構成されているかを判定する関数"
  (boolean (re-matches #"\d+" s)))

(defn extract-last-char-from-path [url]
  "URLの最後の文字列が数字の場合に取得する関数"
  (let [path-id (last (str/split url #"/"))]
    (cond (numeric? path-id) path-id
          :else nil)))

(defn get-value [driver url]
  "取得対象の情報をスクレイピングして取得する関数"
  (let [string (get-string driver url)
        inputs (->  (extract-input-strings string)
                    split-comma-into-list
                    parse-numbers)
        outputs (-> (extract-output-strings string)
                    split-comma-into-list
                    parse-numbers)
        id (extract-last-char-from-path url)]
    (println "Success! Retrieved the following URL: " url)
    {:id id :url url :inputs inputs :outputs outputs}))

(defn main-process [urls not-headless?]
  "スクレイピングを行う関数"
  (let [driver (if not-headless?  (e/chrome) (e/chrome {:args ["--headless"]}))]
    (try
      (login driver)

      (doall (map #(get-value driver %) urls))
      (catch Exception e
        (throw e))
      (finally (e/quit driver)))))

(defn args-empty []
  "引数指定が足りない場合のメッセージを表示する関数"
  (do (println "引数として、少なくとも1つのURLを指定してください。")
      (println "RecursionのURL形式のみ対応しています。")
      (println "RecursionのURL形式:" supported-url-format)
      (println "=== 使い方 ===")
      (println "java -jar Recursion-scraping.jar https://recursionist.io/dashboard/problems/1")))

(defn read-output-filepath []
  ".envからアウトプットファイルパスを読み込む関数"
  (env/env :OUTPUT_FILEPATH))

(defn create-output-filepath [env-path]
  "env-pathの形式の違いによって、デフォルトパスを追加して返す関数"
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
  "引数をパースする関数"
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-options)]
    {:options options
     :arguments arguments
     :summary summary
     :errors errors}))

(defn print-help [parsed-args]
  "helpについて表示する関数"
  (do
    (println (get-in parsed-args [:summary]))
    (println "=== 使いかた ===")
    (println "java -jar Recursion-scraping.jar https://recursionist.io/dashboard/problems/1")
    (println "=== ファイルパス指定の場合(UTF-8のファイルのみ対応) ===")
    (println "java -jar Recursion-scraping.jar input-file.txt")))

(defn slurp-file [filepath]
  "ファイルをUTF-8で読み込む関数"
  (with-open [rdr (io/reader filepath :encoding "UTF-8")]
    (vec (line-seq rdr))))

(defn print-validation-error [args]
  "引数がRecursionのURL形式になっていない旨を表示する関数"
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
      ;; オプションでhelpが指定されているなら、helpを表示して終了する。
      help? (print-help  parsed-args)

      :else (let [validation-error (validate-args args)
                  output-filepath (create-output-filepath (read-output-filepath))]
              (cond
                (and (false? file?) (empty? args)) (args-empty)

                (false? validation-error) (print-validation-error validation-error)

                :else (let [value-map (cond
                                        file? (let  [input-filepath (get-in parsed-args [:options :file])
                                                     args (slurp-file input-filepath)]
                                                (main-process args not-headless?))
                                        :else (main-process args not-headless?))]
                        (write-json-file value-map output-filepath)))))))

;; (-main "-d" "https://recursionist.io/dashboard/problems/1")

