(ns bencode.decoder-test
  (:use clojure.test
        bencode.decoder))

(deftest string-decoding
  (testing "Decoding a non-empty string"
    (is (= "spam"
           (bdecode "4:spam" nil))))

  (testing "Decoding an empty string"
    (is (= ""
           (bdecode "0:" nil))))

  (testing "Decoding a UTF-8 string"
    (is (= "2000\u0445"
           (bdecode (.getBytes "6:2000\u0445" "UTF-8") nil))))

  (testing "Decoding a string that is shorter than expected"
    (is (thrown? IllegalArgumentException
                 (bdecode "4:spa" nil))))

  (testing "Decoding a non-empty string with garbage at the end"
    (is (thrown? IllegalArgumentException
                 (bdecode "4:spam0:" nil)))))

(deftest number-encoding
  (testing "Decoding a positive number"
    (is (= 64
           (bdecode "i64e" nil))))

  (testing "Decoding a very large number"
    (is (= 238273467862384962834523482364273525365425364825376547257N
           (bdecode "i238273467862384962834523482364273525365425364825376547257e" nil))))

  (testing "Decoding a positive number with sign"
    (is (= 64
           (bdecode "i+64e" nil))))

  (testing "Decoding a negative number"
    (is (= -32
           (bdecode "i-32e" nil))))

  (testing "Decoding zero"
    (is (zero?
         (bdecode "i0e" nil))))

  (testing "Decoding octal number"
    (is (thrown? IllegalArgumentException
                 (bdecode "i010e" nil))))

  (testing "Decoding empty number"
    (is (thrown? IllegalArgumentException
                 (bdecode "ie" nil))))

  (testing "Decoding empty number with sign"
    (are [s] (thrown? IllegalArgumentException
                      (bdecode s nil))
         "i-e" "i+e"))

  (testing "Decoding a number with more than one sign"
    (is (thrown? IllegalArgumentException
                 (bdecode "i+-10e" nil))))

  (testing "Decoding invalid 'negative zero'"
    (is (thrown? IllegalArgumentException
                 (bdecode "i-0e" nil))))

  (testing "Decoding multiple numbers inside the same unit"
    (is (thrown? IllegalArgumentException
                 (bdecode "i30-20e" nil)))))

(deftest sequence-decoding
  (testing "Decoding an empty sequence"
    (is (= []
           (bdecode "le" nil))))

  (testing "Decoding a simple non-empty sequence"
    (is (= ["spam" "eggs" 64]
           (bdecode "l4:spam4:eggsi64ee" nil))))

  (testing "Decoding nested sequences"
    (is (= ["spam" ["eggs" 64]]
           (bdecode "l4:spaml4:eggsi64eee" nil)))))

(deftest dict-decoding
  (testing "Use a sorted map"
    (is (sorted? (bdecode "de" nil))))

  (testing "Decoding an empty dictionary"
    (is (= {}
           (bdecode "de" nil))))

  (testing "Decoding a simple non-empty dictionary"
    (is (= {:spam 64 :cow "moo"}
           (bdecode "d3:cow3:moo4:spami64ee" nil))))

  (testing "Decoding a dictionary containing a sequence"
    (is (= {:spam ["eggs" 64]}
           (bdecode "d4:spaml4:eggsi64eee" nil))))

  (testing "Decoding nested dictionaries"
    (is (= {:spam {:cow "moo"}}
           (bdecode "d4:spamd3:cow3:mooee" nil))))

  (testing "Decoding dictionaries using strings as keys instead of keywords"
    (is (= {"spam" {"cow" "moo"}})
        (bdecode "d4:spamd3:cow3:mooee" {:str-keys? true})))

  (testing "Decoding a dictionary with invalid keys"
    (is (thrown? IllegalArgumentException
                 (bdecode "di64e3:cowe" nil)))))

(deftest unsupported-decoding
  (testing "Decoding unknown type"
    (is (thrown? IllegalArgumentException
                 (bdecode "x10" nil)))))
