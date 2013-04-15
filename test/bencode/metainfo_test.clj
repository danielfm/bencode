(ns bencode.metainfo-test
  (:require [clojure.java [io :as io]])
  (:use [clojure.test]
        [bencode.core]
        [bencode.metainfo]))

(defonce ^:dynamic *meta-info* nil)

(defmacro with-torrent [file-name & body]
  `(let [file# (io/file ~file-name)]
     (with-open [stream# (io/input-stream file#)]
       (binding [*meta-info* (parse-metainfo stream#)]
         ~@body))))

(deftest meta-info-type
  (testing "Checking if a torrent only contains a single file"
    (with-torrent "resources/fixtures/single-file.torrent"
      (is (single-file-torrent? *meta-info*))
      (is (not (multi-file-torrent? *meta-info*)))))

  (testing "Checking if a torrent contains multiple files"
    (with-torrent "resources/fixtures/multi-file.torrent"
      (is (multi-file-torrent? *meta-info*))
      (is (not (single-file-torrent? *meta-info*))))))
