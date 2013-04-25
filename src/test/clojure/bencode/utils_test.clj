(ns bencode.utils-test
  (:use clojure.test
        bencode.utils))

(deftest int-flag-from-bool
  (testing "should return 1 if true"
    (is (= 1 (flag-from-bool true))))

  (testing "should return 0 if false"
    (is (zero? (flag-from-bool false)))))

(deftest is-digit
  (testing "Detecting valid digits"
    (doseq [ch (apply str (range 10))]
      (is (digit? (int ch)))))

  (testing "Detecting invalid digits"
    (doseq [ch "azAZ"]
      (is (not (digit? (int ch)))))))
