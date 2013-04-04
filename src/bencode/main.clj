(ns bencode.main
  (:gen-class)
  (:use [bencode.core])
  (:require [clojure.java [io :as io]]))

(def torrent-file (io/file "/Users/danielmartins/Downloads/03.torrent"))

(with-open [stream (io/input-stream torrent-file)]
  (def torrent (bdecode stream {:str-keys? true
                                :raw-keys ["pieces"]})))

(defn parse-date
  ""
  [timestamp]
  (java.util.Date. (* timestamp 1000)))

(defn torrent-length
  ""
  [torrent]
  (get-in torrent ["info" "piece length"]))

(defn torrent-pieces
  ""
  [torrent]
  (/ (count (get-in torrent ["info" "pieces"])) 20))

(defn torrent-first-piece
  ""
  [torrent]
  (take 20 (get-in torrent ["info" "pieces"])))

(defn torrent-last-piece
  ""
  [torrent]
  (let [pieces (get-in torrent ["info" "pieces"])]
    (take 20 (drop (- (count pieces) 20) pieces))))

(defn torrent-size
  ""
  [torrent]
  (* (torrent-length torrent)
     (torrent-pieces torrent)))

(defn hex-from-bytes
  ""
  [byte-arr]
  (let [sb (StringBuffer.)]
    (doseq [b byte-arr]
      (.append sb (.substring (Integer/toString (+ (bit-and b 0xff) 0x100) 16) 1)))
    (.toString sb)))

(defn torrent-info-hash
  ""
  [torrent]
  (let [enc (bencode (get torrent "info") {:raw-str? true})
        dig (java.security.MessageDigest/getInstance "SHA1")
        res (.digest dig enc)]
    (hex-from-bytes res)))

(defn print-announce-group
  ""
  [idx group]
  (print (str (inc idx) ". "))
  (apply print (interpose ", " group))
  (println ""))

(defn print-file
  ""
  [idx file]
  (print (str (inc idx) ". "))
  (print (apply str (interpose "/" (get file "path")))
         (str "[" (get file "length") " bytes]"))
  (println ""))

(defn -main
  [& args]
  (println "Torrent Name........: " (get-in torrent ["info" "name"]))
  (println "Creation Date.......: " (parse-date (get torrent "creation date")))
  (println "Created By..........: " (get torrent "created by"))
  (println "Info Hash...........: " (torrent-info-hash torrent))
  (println "Encoding............: " (get torrent "encoding"))
  (println "Private.............: " (get-in torrent ["info" "private"] 0))
  (println "Comment.............: " (get torrent "comment"))

  (println "")

  (println "Piece Length........: " (str (torrent-length torrent) " bytes"))
  (println "Number of Pieces....: " (torrent-pieces torrent))
  (println "Total size..........: " (str (torrent-size torrent) " bytes"))

  (println "")

  (println "SHA1 of 1st Piece...: " (hex-from-bytes (torrent-first-piece torrent)))
  (println "SHA1 of Last Piece..: " (hex-from-bytes (torrent-last-piece torrent)))

  (println "")

  (println "Trackers............: " (get torrent "announce"))
  (doall
   (map-indexed print-announce-group (get torrent "announce-list")))

  (println "")

  (println "Files...............: " (count (get-in torrent ["info" "files"])))
  (doall
   (map-indexed print-file (get-in torrent ["info" "files"]))))
