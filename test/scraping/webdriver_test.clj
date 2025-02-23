(ns scraping.webdriver-test
  (:require [clojure.test :refer :all]
            [scraping.webdriver :refer :all]))

(deftest detect-os-test
  (testing
   (let [os-name (System/getProperty "os.name")]
     (cond
       (.contains os-name "Mac") (is (= (detect-os) "Mac"))
       (.contains os-name "Linux") (is (= (detect-os) "Linux"))
       (.contains os-name "Windows") (is (= (detect-os) "Windows"))
       :else nil))))

