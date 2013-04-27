(ns bencode.metainfo.writer-test
  (:require [clojure.java.io :as io])
  (:use [clojure.test]
        [bencode.metainfo.writer]))

(deftest torrent-file-path
  (testing "array of file path relative to a given parent directory"
    (let [dir  (io/file "resources/fixtures")
          file (io/file "resources/fixtures/pictures/beach.jpg")]
      (is (= ["pictures" "beach.jpg"]
             (file-path dir file))))))

(deftest torrent-file-entry
  (testing "dictionary denoting a file in multi-file torrent"
    (let [dir  (io/file "resources/fixtures")
          file (io/file "resources/fixtures/pictures/beach.jpg")]
      (is (= {"length" (.length file) "path" (file-path dir file)}
             (file-entry dir file))))))

(deftest torrent-scan-files
  (testing "filtering files from a seq of File"
    (let [all (io/file "resources/fixtures/pictures")]
      (is (every? #(.isFile %) (scan-files all))))))

(def torrent-file-entries
  (testing "list of dictionaries for each file in a multi-file torrent"
    (let [dir (io/file "resources/fixtures/pictures")
          files (scan-files dir)
          entries (torrent-files dir files)]
      (doseq [entry entries]
        (is (> (entry "length") 0))
        (is (not (empty? (entry "path"))))))))

(deftest piece-length
  (testing "default piece length should be 256 KiB"
    (is (= (* 1024 256) (torrent-piece-length nil))))

  (testing "piece length should be 2^n KiB"
    (is (= (* 1024 512) (torrent-piece-length 9)))))

(deftest main-announce-url
  (testing "main announce URL should be the first one"
    (let [urls [["http://group-1.com/1" "http://group-1.com/2"]
                ["http://group-2.com/1"]]]
      (is (= "http://group-1.com/1"
             (torrent-main-announce urls))))))
