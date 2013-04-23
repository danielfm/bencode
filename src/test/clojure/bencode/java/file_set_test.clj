(ns bencode.java.file-set-test
  (:use clojure.test)
  (:require [clojure.java.io :as io])
  (:import [bencode FileSet]))

(def files (filter #(.isFile %) (file-seq (io/file "resources/fixtures/pictures"))))

(deftest file-set-class
  (let [file-set (FileSet. files)]
    (testing "calculating total size"
      (is (= 3377619 (.getTotalSize file-set))))

    (testing "calculating total number of pieces"
      (is (= 13 (.totalPieces file-set (* 256 1024)))))))
