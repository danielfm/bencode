(ns bencode.unsupported-test
  (:use [clojure.test]
        [bencode.core]))

(deftest unsupported-encoding
  (testing "Encoding unsupported data types"
    (are [type] (thrown? IllegalArgumentException (bencode type))
         (float 10) (double 10) (bigdec 10))))

(deftest unsupported-decoding
  (testing "Decoding unknown type"
    (is (thrown? IllegalArgumentException
                 (bdecode "x10")))))
