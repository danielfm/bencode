(ns bencode.java.piece-digest-job-test
  (:use clojure.test)
  (:require [clojure.java.io :as io])
  (:import [bencode PieceDigestJob]
           [java.util.concurrent.atomic AtomicInteger]))

(def data (byte-array (map byte (range 10))))

(deftest piece-hashing
  (testing "hashing first piece"
    (let [global-hash (byte-array 40)
          hashed-pieces (AtomicInteger.)
          job (PieceDigestJob. global-hash data (count data) 0 hashed-pieces)
          _ (.run job)]
      (testing "increments the number of hashed pieces"
        (is (= 1 (.get hashed-pieces))))

      (testing "writes the digest result to the correct area of the global hash"
        (is (= '(73 65 121 113 74 108 -42 39 35 -99 -2 -34 -33 45 -23 -17 -103 76 -81 3
                 0  0  0   0   0  0   0   0  0  0   0  0   0   0  0   0   0    0  0   0)
               (seq global-hash))))))

  (testing "hashing incomplete last piece"
    (let [global-hash (byte-array 40)
          hashed-pieces (AtomicInteger. 1)
          job (PieceDigestJob. global-hash data (/ (count data) 2) 1 hashed-pieces)
          _ (.run job)]
      (testing "increments the number of hashed pieces"
        (is (= 2 (.get hashed-pieces))))

      (testing "writes the digest result to the correct area of the global hash"
        (is (= '(0  0   0  0  0  0  0  0  0   0   0   0  0    0    0    0   0    0    0   0
                 28 -14 81 71 45 89 -8 -6 -34 -77 -85 37 -114 -112 -103 -99 -124 -111 -66 25)
               (seq global-hash)))))))
