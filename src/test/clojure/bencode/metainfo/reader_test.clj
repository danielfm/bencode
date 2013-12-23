(ns bencode.metainfo.reader-test
  (:require [clojure.java [io :as io]])
  (:use [clojure.test]
        [bencode.core]
        [bencode.metainfo.reader])
  (:import [java.util Date]))

(defonce ^:dynamic *meta-info* nil)

(defmacro with-torrent [file-name & body]
  `(binding [*meta-info* (parse-metainfo-file ~file-name)]
     ~@body))

(deftest meta-info-type
  (testing "Checking if a torrent only contains a single file"
    (with-torrent "resources/fixtures/single-file.torrent"
      (is (single-file-torrent? *meta-info*))
      (is (not (multi-file-torrent? *meta-info*)))))

  (testing "Checking if a torrent contains multiple files"
    (with-torrent "resources/fixtures/multi-file.torrent"
      (is (multi-file-torrent? *meta-info*))
      (is (not (single-file-torrent? *meta-info*))))))

(deftest basic-info
  (with-torrent "resources/fixtures/single-file.torrent"
    (testing "Tracker URL"
      (is (= "udp://tracker.1337x.org:80/announce"
             (torrent-announce *meta-info*))))

    (testing "Tracker URL list"
      (is (= [["udp://tracker.1337x.org:80/announce"]
              ["udp://tracker.publicbt.com:80/announce"]
              ["udp://tracker.openbittorrent.com:80/announce"]
              ["udp://fr33domtracker.h33t.com:3310/announce"]
              ["udp://tracker.istole.it:80/announce"]
              ["http://exodus.desync.com:6969/announce"]
              ["udp://tracker.ccc.de:80"]
              ["udp://tracker.istole.it:6969"]
              ["udp://tracker.openbittorrent.com:80"]
              ["udp://tracker.publicbt.com:80"]]
             (torrent-announce-list *meta-info*))))

    (testing "Torrent name"
      (is (= "ubuntu-12.10-desktop-i386.iso" (torrent-name *meta-info*))))

    (testing "Piece length, in bytes"
      (is (= 524288 (torrent-piece-length *meta-info*))))

    (testing "Number of pieces"
      (is (= 1507 (torrent-pieces-count *meta-info*))))

    (testing "Pieces hash"
      (testing "Hash for the first piece"
        (is (= "0fb30249ec80648eebe61636396d9a247068d5a2"
               (first (torrent-pieces-hash-list *meta-info*)))))

      (testing "Hash for the last piece"
        (is (= "ad649e2955576e0af98200564cd4c5e2da081453"
               (last (torrent-pieces-hash-list *meta-info*))))))

    (testing "Creation date"
      (is (= (Date. (* 1350570562 1000))
             (torrent-creation-date *meta-info*))))

    (testing "Comment"
      (is (= "http://www.monova.org" (torrent-comment *meta-info*))))

    (testing "Private"
      (is (public-torrent? *meta-info*))
      (is (not (private-torrent? *meta-info*))))))

(deftest private-torrent-info
  (with-torrent "resources/fixtures/private.torrent"
    (testing "Private"
      (is (private-torrent? *meta-info*))
      (is (not (public-torrent? *meta-info*))))

    (testing "Created by"
      (is (= "mktorrent 1.0" (torrent-created-by *meta-info*))))))

(deftest single-file-torrent-info
  (with-torrent "resources/fixtures/single-file.torrent"
    (testing "Torrent size"
      (is (= 789884928 (torrent-size *meta-info*))))

    (testing "Torrent info hash as a byte array"
      (let [hash (torrent-info-hash *meta-info*)]
        (is (instance? (Class/forName "[B") hash))
        (is (= '(51 89 -112 -42 21 89 75 -101 -28 9 -52 -2 -71 88 100 -30 78 -57 2 -57)
               (seq hash)))))

    (testing "Torrent info hash as string"
      (is (= "335990d615594b9be409ccfeb95864e24ec702c7"
             (torrent-info-hash-str *meta-info*))))))

(deftest multi-file-torrent-info
  (with-torrent "resources/fixtures/multi-file.torrent"
    (testing "Torrent size is the sum of the sizes of all files"
      (is (= 5805771164 (torrent-size *meta-info*))))

    (testing "Torrent info hash as a byte array"
      (let [hash (torrent-info-hash *meta-info*)]
        (is (instance? (Class/forName "[B") hash))
        (is (= '(-124 -118 106 14 -58 -56 85 7 -72 55 14 -105 -101 19 50 20 -27 -75 -90 -44)
               (seq hash)))))

    (testing "Torrent info hash as string"
      (is (= "848a6a0ec6c85507b8370e979b133214e5b5a6d4"
             (torrent-info-hash-str *meta-info*))))

    (testing "Torrent files"
      (is (= [{"length" 4353378304, "path" ["CentOS-6.4-x86_64-bin-DVD1.iso"]}
              {"length" 1452388352, "path" ["CentOS-6.4-x86_64-bin-DVD2.iso"]}
              {"length" 261,  "path" ["md5sum.txt"]}
              {"length" 1135, "path" ["md5sum.txt.asc"]}
              {"length" 293,  "path" ["sha1sum.txt"]}
              {"length" 1167, "path" ["sha1sum.txt.asc"]}
              {"length" 389,  "path" ["sha256sum.txt"]}
              {"length" 1263, "path" ["sha256sum.txt.asc"]}]
             (torrent-files *meta-info*))))))

(deftest magnet-link
  (with-torrent "resources/fixtures/multi-file.torrent"
    (testing "Magnet link generation"
      (is (= "magnet:?xt=urn:btih:848a6a0ec6c85507b8370e979b133214e5b5a6d4&dn=CentOS-6.4-x86_64-bin-DVD1to2&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Ffr33domtracker.h33t.com%3A3310%2Fannounce&tr=udp%3A%2F%2Ftracker.istole.it%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.feednet.ro%3A80%2Fannounce&tr=http%3A%2F%2Fipv6.torrent.centos.org%3A6969%2Fannounce&tr=http%3A%2F%2Ftorrent.centos.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.ccc.de%3A80&tr=udp%3A%2F%2Ftracker.istole.it%3A6969&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80"
             (torrent-magnet-link *meta-info*))))))
