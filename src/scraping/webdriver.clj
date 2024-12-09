(ns scraping.webdriver
  (:import [org.openqa.selenium WebDriver]
           [io.github.bonigarcia.wdm WebDriverManager]
           [org.openqa.selenium.chrome ChromeDriver]
           [org.openqa.selenium.chrome ChromeOptions]))

(defn get-browser-path []
  (let [manager (WebDriverManager/chromedriver)
        browser-path (.toString (.get (.getBrowserPath manager)))]
    browser-path))

;; ---

(defn start-selenium-browser []
  (let [options (ChromeOptions.)]
    (.setBinary options "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    (let [driver (ChromeDriver. options)]
      (try
        (.get driver "https://www.google.com")
        (println "Page title:" (.getTitle driver))
        (finally
          (.quit driver))))))

;; (start-selenium-browser)

;; (println (get-browser-path))


(defn setup-chromedriver []
  (let [manager (WebDriverManager/chromedriver)]
    (.browserPath manager "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    ;; (.driverVersion manager "131.0.6778.87")
    (.setup manager)
    (let [path (.getDownloadedDriverPath manager)]
      (println "ChromeDriver setup completed.")
      (println "Download version: " (.getDownloadedDriverVersion manager))
      path)))

;; (println (setup-chromedriver))

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

;; (start-browser)


;; ---