(ns bencode.java.parallel-piece-digest-test
  (:use clojure.test)
  (:require [clojure.java.io :as io])
  (:import [bencode ParallelPieceDigest]
           [java.io File]))

(def piece-length (* 256 1024))

(deftest parallel-piece-digest
  (testing "digesting file that fit in a piece"
    (let [files [(io/file "resources/fixtures/pictures/arches-9.jpg")]
          digest (ParallelPieceDigest. files piece-length)
          hash (.computeHash digest 2)]

      (testing "checking the number of pieces"
        (is (= 1 (/ (count hash) 20))))

      (testing "checking the generated hash"
        (is (= '(-90 -89 102 2 93 54 -53 -92 -26 -103 -95 -126 -39 -103 -75 -78 -80 79 19 -3)
               (seq hash))))))

  (testing "digesting two files that fit in a piece"
    (let [files [(io/file "resources/fixtures/pictures/arches-9.jpg")
                 (io/file "resources/fixtures/pictures/beach.jpg")]
          digest (ParallelPieceDigest. files piece-length)
          hash (.computeHash digest 2)]

      (testing "checking the number of pieces"
        (is (= 1 (/ (count hash) 20))))

      (testing "checking the generated hash"
        (is (= '(-30 -39 -91 -60 85 95 5 -89 -67 87 -23 45 41 -72 -118 120 74 84 124 -21)
               (seq hash))))))

  (testing "digesting a file that don't fit in a piece"
    (let [files [(io/file "resources/fixtures/pictures/architecture.jpg")]
          digest (ParallelPieceDigest. files piece-length)
          hash (.computeHash digest 2)]

      (testing "checking the number of pieces"
        (is (= 12 (/ (count hash) 20))))

      (testing "checking the generated hash"
        (is (= '(39 -75 90 106 -7 4 -113 -116 -51 94 75 4 -18 49 -120 -13 56 99 -17 35
                 50 -73 72 -100 85 -16 105 -28 -30 26 52 -71 76 -3 -76 105 69 -28 36 64
                 94 -89 26 4 -15 -96 71 106 121 79 -83 119 49 94 123 -65 93 42 46 85
                 -104 -59 88 -47 -31 68 9 77 -101 -94 75 -93 -21 105 78 72 -18 -93 -5 34
                 -100 -16 49 87 47 -78 -55 117 35 -42 -100 42 93 115 56 83 -126 37 103 105
                 -63 -37 -96 -117 -76 -96 34 -33 -19 111 -83 55 -48 -20 69 -27 -43 -59 60 118
                 41 -66 -90 8 83 4 -23 27 -89 127 36 -65 93 17 -69 105 19 56 -53 93
                 -42 9 115 118 0 10 -61 106 -37 -13 -59 77 -1 89 2 86 -97 121 22 19
                 -126 -83 -72 -2 -7 -3 -17 11 22 13 124 2 -95 -107 75 92 -25 48 -112 67
                 11 63 -3 -78 76 -101 -26 102 6 23 38 -13 67 102 112 -70 -78 -69 91 13
                 -120 -61 -52 -110 -16 96 84 57 19 -76 44 -60 -89 41 57 -71 45 6 82 64
                 -112 54 -24 -86 -15 117 -102 -100 38 -25 -126 12 31 -93 -89 21 -25 -26 114 -108)
               (seq hash)))))))
