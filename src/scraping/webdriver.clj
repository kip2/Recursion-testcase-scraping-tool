(ns scraping.webdriver
  (:import [org.openqa.selenium WebDriver]
           [io.github.bonigarcia.wdm WebDriverManager]
           [org.openqa.selenium.chrome ChromeDriver]
           [org.openqa.selenium.chrome ChromeOptions])
  (:require
   [etaoin.api :as e]))

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

(defn get-chrome-version []
  (let [os-name (detect-os)
        cmd (cond
              (= os-name "Mac") [""]
              (= os-name "Widnwos") [""]
              :else ["google-chrome" "--version"])]))


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


