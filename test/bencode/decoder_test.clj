(ns bencode.decoder-test
  (:use clojure.test
        bencode.decoder))

(deftest string-decoding
  (testing "Decoding a non-empty string"
    (is (= "spam" (bdecode "4:spam"))))

  (testing "Decoding an empty string"
    (is (= "" (bdecode "0:"))))

  (testing "Decoding a string that is shorter than expected"
    (is (thrown? IllegalArgumentException (bdecode "4:spa"))))

  (testing "Decoding a non-empty string with garbage at the end"
    (is (thrown? IllegalArgumentException (bdecode "4:spam0:")))))

(deftest number-encoding
  (testing "Decoding a positive number"
    (is (= 64 (bdecode "i64e"))))

  (testing "Decoding a positive number with sign"
    (is (= 64 (bdecode "i+64e"))))

  (testing "Decoding a negative number"
    (is (= -32 (bdecode "i-32e"))))

  (testing "Decoding zero"
    (is (zero? (bdecode "i0e"))))

  (testing "Decoding empty number"
    (is (thrown? IllegalArgumentException (bdecode "ie"))))

  (testing "Decoding empty number with sign"
    (are [s] (thrown? IllegalArgumentException (bdecode s))
         "i-e" "i+e"))

  (testing "Decoding a number with more than one sign"
    (is (thrown? IllegalArgumentException (bdecode "i+-10e"))))

  (testing "Decoding invalid 'negative zero'"
    (is (thrown? IllegalArgumentException (bdecode "i-0e"))))

  (testing "Decoding multiple numbers inside the same unit"
    (is (thrown? IllegalArgumentException (bdecode "i30-20e")))))

(deftest sequence-decoding
  (testing "Decoding an empty sequence"
    (is (= [] (bdecode "le"))))

  (testing "Decoding a simple non-empty sequence"
    (is (= ["spam" "eggs" 64] (bdecode "l4:spam4:eggsi64ee"))))

  (testing "Decoding nested sequences"
    (is (= ["spam" ["eggs" 64]] (bdecode "l4:spaml4:eggsi64eee")))))

(deftest dict-decoding
  (testing "Use a sorted map"
    (is (sorted? (bdecode "de"))))

  (testing "Decoding an empty dictionary"
    (is (= {} (bdecode "de"))))

  (testing "Decoding a simple non-empty dictionary"
    (is (= {:spam 64 :cow "moo"} (bdecode "d3:cow3:moo4:spami64ee"))))

  (testing "Decoding a dictionary containing a sequence"
    (is (= {:spam ["eggs" 64]} (bdecode "d4:spaml4:eggsi64eee"))))

  (testing "Decoding nested dictionaries"
    (is (= {:spam {:cow "moo"}} (bdecode "d4:spamd3:cow3:mooee"))))

  (testing "Decoding a dictionary with invalid keys"
    (is (thrown? IllegalArgumentException (bdecode "di64e3:cowe")))))

(deftest unsupported-decoding
  (testing "Decoding unknown type"
    (is (thrown? IllegalArgumentException (bdecode "x10")))))
