(ns bencode.number-test
  (:use [clojure.test]
        [bencode.core]))

(deftest encoding-unsigned-numbers
  (testing "Encoding zero"
    (is (= "i0e"
           (bencode 0))))

  (testing "Encoding a positive number"
    (is (= "i64e"
           (bencode 64))))

  (testing "Encoding a very large number"
    (are [n] (= "i238273467862384962834523482364273525365425364825376547257e"
                (bencode n))
         (BigInteger. "238273467862384962834523482364273525365425364825376547257")
         238273467862384962834523482364273525365425364825376547257N)))

(deftest encoding-signed-numbers
  (testing "Encoding a negative number"
    (is (= "i-32e"
           (bencode -32)))))

(deftest decoding-unsigned-numbers
  (testing "Decoding zero"
    (is (zero?
         (bdecode "i0e"))))

  (testing "Decoding a positive number"
    (is (= 64
           (bdecode "i64e"))))

  (testing "Decoding a very large number"
    (is (= 238273467862384962834523482364273525365425364825376547257N
           (bdecode "i238273467862384962834523482364273525365425364825376547257e")))))

(deftest decoding-signed-numbers
  (testing "Decoding a positive number with sign"
    (is (= 64
           (bdecode "i+64e"))))

  (testing "Decoding a negative number"
    (is (= -32
           (bdecode "i-32e")))))

(deftest decoding-invalid-numbers
  (testing "Decoding octal number"
    (is (thrown? IllegalArgumentException
                 (bdecode "i010e"))))

  (testing "Decoding empty number"
    (is (thrown? IllegalArgumentException
                 (bdecode "ie"))))

  (testing "Decoding empty number with sign"
    (are [s] (thrown? IllegalArgumentException
                      (bdecode s))
         "i-e" "i+e"))

  (testing "Decoding a number with more than one sign"
    (is (thrown? IllegalArgumentException
                 (bdecode "i+-10e"))))

  (testing "Decoding invalid 'negative zero'"
    (is (thrown? IllegalArgumentException
                 (bdecode "i-0e"))))

  (testing "Decoding multiple numbers inside the same unit"
    (is (thrown? IllegalArgumentException
                 (bdecode "i30-20e"))))

  (testing "Decoding number with invalid terminator"
    (is (thrown? IllegalArgumentException
                 (bdecode "i10x")))))
