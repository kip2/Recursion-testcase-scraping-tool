(ns scraping.core-test
  (:require [clojure.test :refer :all]
            [scraping.core :refer :all]))

(deftest extract-after-allow-test
  (testing
   (is (= (extract-after-allow "--> test") "test"))
    (is (= (extract-after-allow "") nil))
    (is (= (extract-after-allow "-->") nil))))

(deftest extract-inside-parens-test
  (testing
   (is (= (extract-inside-parens "(aaa)") "aaa"))
    (is (= (extract-inside-parens "(a,b,c)") "a,b,c"))
    (is (= (extract-inside-parens "") nil))
    (is (= (extract-inside-parens "()") ""))))

(deftest validate-url-test
  (testing
   (is (= (validate-url "https://validation.io/") false))
    (is (= (validate-url "https://recursionist.io/dashboard/problems") false))
    (is (= (validate-url "https://recursionist.io/dashboard/problems/") true))
    (is (= (validate-url "https://recursionist.io/dashboard/problems/123/test/validation/123") true))
    (is (= (validate-url "https://recursionist.io/dashboard/problems/123") true))))

(deftest validate-args-test
  (testing
   (let [true-args ["https://recursionist.io/dashboard/problems/1"
                    "https://recursionist.io/dashboard/problems/2"
                    "https://recursionist.io/dashboard/problems/3"
                    "https://recursionist.io/dashboard/problems/4"
                    "https://recursionist.io/dashboard/problems/5"]
         empty-args nil
         false-args ["https://recursionist.io/dashboard/problems/1"
                     "https://validation.io/dashboard/problems/2"
                     "https://recursionist.io/dashboard/problems/3"]]
     (is (= (validate-args true-args) true))
     (is (= (validate-args false-args) "https://validation.io/dashboard/problems/2"))
     (is (= (validate-args empty-args) true)))))


