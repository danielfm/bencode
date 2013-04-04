(ns bencode.utils-test
  (:use clojure.test
        bencode.utils))

(deftest is-digit
  (testing "Detecting valid digits"
    (doseq [ch (apply str (range 10))]
      (is (digit? (int ch)))))

  (testing "Detecting invalid digits"
    (doseq [ch "azAZ"]
      (is (not (digit? (int ch)))))))
