(ns bencode.encoder-test
  (:use clojure.test
        bencode.encoder))

(deftest string-encoding
  (testing "Encoding a non-empty string"
    (is (= "4:spam"
           (bencode "spam"))))

  (testing "Encoding an empty string"
    (is (= "0:"
           (bencode "")))))

(deftest keyword-encoding
  (testing "Encoding a keyword"
    (is (= "4:spam"
           (bencode :spam)))))

(deftest number-encoding
  (testing "Encoding a positive number"
    (is (= "i64e"
           (bencode 64))))

  (testing "Encoding a negative number"
    (is (= "i-32e"
           (bencode -32))))

  (testing "Encoding zero"
    (is (= "i0e"
           (bencode 0)))))

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

(deftest dict-encoding
  (testing "Encoding an empty dictionary"
    (is (= "de" (bencode {}))))

  (testing "Encoding a simple non-empty dictionary"
    (is (= "d3:cow3:moo4:spami64ee" (bencode {:spam 64, :cow "moo"}))))

  (testing "Encoding a dictionary containing a sequence"
    (is (= "d4:spaml4:eggsi64eee" (bencode {:spam ["eggs" 64]}))))

  (testing "Encoding nested dictionaries"
    (is (= "d4:spamd3:cow3:mooee"
           (bencode {:spam {:cow "moo"}}))))

  (testing "Encoding a dictionary with invalid keys"
    (are [key] (thrown? IllegalArgumentException (bencode {key "val"}))
         [] {} 64)))

(deftest unsupported-encoding
  (testing "Encoding unsupported data types"
    (are [type] (thrown? IllegalArgumentException (bencode type))
         (float 10) (double 10) (bigdec 10))))
