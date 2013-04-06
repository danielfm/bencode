(ns bencode.main
  (:gen-class)
  (:use [bencode.core])
  (:require [clojure.java [io :as io]])
  (:import [java.security MessageDigest]
           [java.util Date]
           [java.text SimpleDateFormat]))

(defn format-date
  [timestamp]
  (let [f (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSZ")]
    (.format f (Date. (* timestamp 1000)))))

(defn torrent-piece-length
  [torrent]
  (get-in torrent ["info" "piece length"]))

(defn torrent-pieces
  [torrent]
  (get-in torrent ["info" "pieces"]))

(defn count-torrent-pieces
  [torrent]
  (/ (count (torrent-pieces torrent)) 20))

(defn torrent-size
  [torrent]
  (* (torrent-piece-length torrent)
     (count-torrent-pieces torrent)))

(defn hex-from-bytes
  [byte-arr]
  (let [sb (StringBuffer.)]
    (doseq [b byte-arr]
      (.append sb (.substring (Integer/toString (+ (bit-and b 0xff) 0x100) 16) 1)))
    (.toString sb)))

(defn torrent-info-hash
  [torrent]
  (let [enc (bencode (get torrent "info") {:raw-str? true})
        dig (MessageDigest/getInstance "SHA1")
        res (.digest dig enc)]
    (hex-from-bytes res)))

(defn print-announce-group
  [idx group]
  (print (str (inc idx) ". "))
  (apply print (interpose ", " group))
  (println ""))

(defn print-file
  [idx file]
  (print (str (inc idx) ". "))
  (print (apply str (interpose "/" (get file "path")))
         (str "[" (get file "length") " bytes]"))
  (println ""))

(defn -main
  [& args]
  (let [f (io/file (nth args 0))]
    (with-open [stream (io/input-stream f)]
      (let [torrent(bdecode stream {:str-keys? true
                                    :raw-keys ["pieces"]})]
          (println "Torrent Name..: " (get-in torrent ["info" "name"]))
          (println "Creation Date.: " (format-date (get torrent "creation date")))
          (println "Created By....: " (get torrent "created by"))
          (println "Info Hash.....: " (torrent-info-hash torrent))
          (println "Private.......: " (get-in torrent ["info" "private"] 0))
          (println "Comment.......: " (get torrent "comment"))

          (println "")

          (println "Piece Length..: " (str (torrent-piece-length torrent) " bytes"))
          (println "# of Pieces...: " (count-torrent-pieces torrent))
          (println "Total Size....: " (str (torrent-size torrent) " bytes"))

          (println "")

          (println "Trackers......: " (get torrent "announce"))
          (doall
           (map-indexed print-announce-group (get torrent "announce-list")))

          (println "")

          (println "Files.........: " (count (get-in torrent ["info" "files"])))
          (doall
           (map-indexed print-file (get-in torrent ["info" "files"])))

          (println "")))))
