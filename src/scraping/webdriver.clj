(ns scraping.webdriver
  (:import [org.openqa.selenium WebDriver]
           [io.github.bonigarcia.wdm WebDriverManager]
           [org.openqa.selenium.chrome ChromeDriver]
           [org.openqa.selenium.chrome ChromeOptions])
  (:require
   [etaoin.api :as e]
   [clojure.java.shell :refer [sh]]
   [clj-http.client :as client]))

(defn get-browser-path
  "ブラウザのローカルパスを取得する関数"
  ;; todo: Windows環境でテストをして、どのように取得できるかを確認する
  ;; todo: Linux環境でテストをして、どのように取得できるかを確認する
  []
  (let [manager (WebDriverManager/chromedriver)
        browser-path (.toString (.get (.getBrowserPath manager)))]
    browser-path))

(defn detect-os []
;; todo: Windows環境でテストコードをテストする 
;; todo: Linux環境でテストコードをテストする 
  (let [os-name (System/getProperty "os.name")]
    (cond
      (.contains os-name "Mac") "Mac"
      (.contains os-name "Linux") "Linux"
      (.contains os-name "Windows") "Windows"
      :else (throw (Exception. (str "Unsupported OS: " os-name))))))

(defn get-latest-chromedriver-version [chrome-version]
  (let [url (str "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json")
        response (client/get url {:as :json})
        data (:body response)]
    (get-in data ["channels" "Stable" "version"])))

(defn get-chrome-version
  "インストールしているchromeのバージョンを取得する関数"
  []
  (let [os-name (detect-os)
        browser-path (get-browser-path)
        cmd (cond
              (= os-name "Mac") [browser-path "--version"]
              (= os-name "Widnwos") [browser-path ""]
              :else ["google-chrome" "--version"])
        output (:out (apply sh cmd))]
    (when output
      (re-find #"\d+\.\d+\.\d+" output))))

(defn start-selenium-browser []
  (let [options (ChromeOptions.)]
    (.setBinary options "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    (let [driver (ChromeDriver. options)]
      (try
        (.get driver "https://www.google.com")
        (println "Page title:" (.getTitle driver))
        (finally
          (.quit driver))))))

(defn setup-chromedriver []
  (let [manager (WebDriverManager/chromedriver)]
    (.browserPath manager "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    ;; (.driverVersion manager "131.0.6778.87")
    (.setup manager)
    (let [path (.getDownloadedDriverPath manager)]
      (println "ChromeDriver setup completed.")
      (println "Download version: " (.getDownloadedDriverVersion manager))
      path)))

(defn start-browser []
  (let [driver-path (setup-chromedriver)
        driver (e/chrome {:path-driver driver-path})]
    (try
      ;; Googleにアクセスしてページタイトルを取得
      (e/go driver "https://www.google.com")
      (println "Page title:" (e/get-title driver))
      ;; 他の操作を追加可能
      (finally
        (e/quit driver))))) ;; ブラウザを終了


