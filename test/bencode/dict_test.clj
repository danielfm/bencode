(ns bencode.dict-test
  (:use [clojure.test]
        [bencode.core]))

(deftest dict-encoding
  (testing "Encoding an empty dictionary"
    (is (= "de" (bencode {}))))

  (testing "Encoding a simple non-empty dictionary"
    (is (= "d3:cow3:moo3:onei2e4:spami64ee"
           (bencode {"spam" 64, "cow" "moo", :one 2}))))

  (testing "Encoding a dictionary containing a sequence"
    (is (= "d4:spaml4:eggsi64eee"
           (bencode {:spam ["eggs" 64]}))))

  (testing "Encoding nested dictionaries"
    (is (= "d4:spamd3:cow3:mooee"
           (bencode {:spam {:cow "moo"}}))))

  (testing "Encoding a dictionary with invalid keys"
    (are [key] (thrown? IllegalArgumentException (bencode {key "val"}))
         [] {} 64)))

(deftest dict-decoding
  (testing "Use a sorted map"
    (is (sorted? (bdecode "de"))))

  (testing "Decoding an empty dictionary"
    (is (= {}
           (bdecode "de"))))

  (testing "Decoding a simple non-empty dictionary"
    (is (= {:spam 64 :cow "moo"}
           (bdecode "d3:cow3:moo4:spami64ee"))))

  (testing "Decoding a dictionary containing a sequence"
    (is (= {:spam ["eggs" 64]}
           (bdecode "d4:spaml4:eggsi64eee"))))

  (testing "Decoding nested dictionaries"
    (is (= {:spam {:cow "moo"}}
           (bdecode "d4:spamd3:cow3:mooee"))))

  (testing "Decoding a dictionary with invalid keys"
    (is (thrown? IllegalArgumentException
                 (bdecode "di64e3:cowe")))))

(deftest decoding-options
  (testing "Decoding keys as strings instead of keywords"
    (is (= {"spam" {"cow" "moo"}})
        (bdecode "d4:spamd3:cow3:mooee" {:str-keys? true})))

  (testing "Decoding dict value as byte array"
    (let [result (bdecode "d3:cow3:moo3:onei2e4:spami64ee" {:raw-keys ["cow"]})]
      (is (= 2  (:one result)))
      (is (= 64 (:spam result)))
      (is (= (vec (byte-array (map byte "moo")))
             (vec (:cow result)))))))
