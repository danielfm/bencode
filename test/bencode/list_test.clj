(ns bencode.list-test
  (:use [clojure.test]
        [bencode.core]))

(deftest sequence-decoding
  (testing "Decoding an empty sequence"
    (is (= []
           (bdecode "le"))))

  (testing "Decoding a simple non-empty sequence"
    (is (= ["spam" "eggs" 64]
           (bdecode "l4:spam4:eggsi64ee"))))

  (testing "Decoding nested sequences"
    (is (= ["spam" ["eggs" 64]]
           (bdecode "l4:spaml4:eggsi64eee")))))

(deftest sequence-encoding
  (testing "Encoding an empty sequence"
    (is (= "le"
           (bencode []))))

  (testing "Encoding a simple non-empty sequence"
    (is (= "l4:spam4:eggsi64ee"
           (bencode (seq ["spam" "eggs" 64])))))

  (testing "Encoding nested sequences"
    (is (= "l4:spaml4:eggsi64eee"
           (bencode #{"spam" ["eggs" 64]})))))

