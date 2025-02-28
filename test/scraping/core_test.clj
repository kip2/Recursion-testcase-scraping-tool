(ns scraping.core-test
  (:require [clojure.test :refer :all]
            [scraping.core :refer :all]))

(deftest extract-last-char-from-path-test
  (testing
   (is (= (extract-last-char-from-path "https://aaa/bbb/ccc/") nil))
    (is (= (extract-last-char-from-path "https://aaa/bbb/ccc") nil))
    (is (= (extract-last-char-from-path "https://aaa/bbb/ccc/1") "1"))))

(deftest numeric?-test
  (testing
   (is (= (numeric? "123") true))
    (is (= (numeric? "-123") false))
    (is (= (numeric? "0") true))
    (is (= (numeric? "-0") false))
    (is (= (numeric? "001") true))
    (is (= (numeric? "-001") false))
    (is (= (numeric? "abc") false))
    (is (= (numeric? "12a3") false))
    (is (= (numeric? "") false))
    (is (= (numeric? "-") false))
    (is (= (numeric? "2") true))))


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

(deftest split-comma-into-list-test
  (testing
   (is (= (split-comma-into-list ["1,2,3" "3,10"]) [["1" "2" "3"] ["3" "10"]]))
    (is (= (split-comma-into-list ["123" "310"]) ["123" "310"]))
    (is (= (split-comma-into-list ["" ""]) ["" ""]))))

(deftest parse-numbers-test
  (testing
   (is (= (parse-numbers ["1" "2" "3"]) [1 2 3]))
    (is (= (parse-numbers ["1.2" "2.5" "3.4"]) [1.2 2.5 3.4]))
    (is (= (parse-numbers ["1" "2.3" "3"]) [1 2.3 3]))
    (is (= (parse-numbers ["1" "2.3" "Hello" "abc"]) [1 2.3 "Hello" "abc"]))
    (is (= (parse-numbers ["" ""]) ["" ""]))
    (is (= (parse-numbers ["1e10" "NaN"]) ["1e10" "NaN"]))
    (is (= (parse-numbers []) []))
    (is (= (parse-numbers nil) '())))

  (testing "Nested list inputs"
    (is (= (parse-numbers [["1" "2"] ["3.4" "text"]]) [[1 2] [3.4 "text"]]))
    (is (= (parse-numbers [["42" ["nested" ["1" "2"]]]]) [[42 ["nested" [1 2]]]]))

    (testing "Flat list inputs with signed numbers"
      (is (= (parse-numbers ["-1" "+2" "3"]) [-1 2 3]))
      (is (= (parse-numbers ["-2.3" "+3.4"]) [-2.3 3.4])))

    (testing "Nested list inputs with signed numbers"
      (is (= (parse-numbers [["-1" "+2"] ["-3.4" "+4.5"]]) [[-1 2] [-3.4 4.5]])))

    (testing "Special cases"
      (is (= (parse-numbers ["--1" "++2.3" "NaN"]) ["--1" "++2.3" "NaN"])))))

(deftest create-output-filepath-test
  (testing
   (is (= (create-output-filepath nil) "./testcase.json"))
    (is (= (create-output-filepath "./a/b/c/test.json") "./a/b/c/test.json"))
    (is (= (create-output-filepath "./a/b/c/") "./a/b/c/testcase.json"))
    (is (= (create-output-filepath "") "./testcase.json"))))
   