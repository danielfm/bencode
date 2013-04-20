(ns bencode.type.string-test
  (:use [clojure.test]
        [bencode.core]))

(deftest string-encoding
  (testing "Encoding a non-empty string"
    (is (= "4:spam"
           (bencode "spam"))))

  (testing "Encoding an empty string"
    (is (= "0:"
           (bencode ""))))

  (testing "Encoding an string respecing the encoding"
    (is (= "6:2000\u0445"
           (bencode "2000\u0445")))))

(deftest bytes-encoding
  (testing "Encoding a byte-array"
    (is (= "4:spam"
           (bencode (.getBytes "spam"))))))

(deftest keyword-encoding
  (testing "Encoding a keyword"
    (is (= "4:spam"
           (bencode :spam)))))

(deftest string-decoding
  (testing "Decoding a non-empty string"
    (is (= "spam"
           (bdecode "4:spam"))))

  (testing "Decoding an empty string"
    (is (= ""
           (bdecode "0:"))))

  (testing "Decoding a UTF-8 string"
    (is (= "2000\u0445"
           (bdecode (.getBytes "6:2000\u0445" "UTF-8")))))

  (testing "Decoding a string that is shorter than expected"
    (is (thrown? IllegalArgumentException
                 (bdecode "4:spa"))))

  (testing "Decoding a non-empty string with garbage at the end"
    (is (thrown? IllegalArgumentException
                 (bdecode "4:spam0:")))))

(deftest decoding-options
  (testing "Decoding strings as a byte array"
    (is (= (vec (.getBytes "spam"))
           (vec (bdecode "4:spam" {:raw-str? true}))))))
