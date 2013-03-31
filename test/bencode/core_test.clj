(ns bencode.core-test
  (:use clojure.test
        bencode.core))

(deftest delegate-fns
  (testing "Function that calls bencode"
    (is (= "1:x" (bencode "x"))))

  (testing "Function that calls bdecode"
    (is (= "x" (bdecode "1:x")))))
