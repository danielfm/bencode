(ns bencode.metainfo.reader
  (:use [bencode.core])
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [java.net URLEncoder]
           [java.security MessageDigest]
           [java.util Date]
           [org.apache.commons.codec.binary Base32]))

(defn- hex-from-bytes
  "Converts a byte-array to a hex string."
  [byte-arr]
  (let [sb (StringBuffer.)]
    (doseq [b byte-arr]
      (.append sb (.substring (Integer/toString (+ (bit-and b 0xff) 0x100) 16) 1)))
    (.toString sb)))

(defn parse-metainfo
  "Bdecodes the given torrent metainfo input."
  [in]
  (bdecode in {:str-keys? true
               :raw-keys ["pieces"]}))

(defn parse-metainfo-file
  "Bdecodes the torrent metainfo located at file-path."
  [file-path]
  (with-open [in (io/input-stream file-path)]
    (parse-metainfo in)))

(defn single-file-torrent?
  "Returns whether metainfo represents a single-file torrent."
  [metainfo]
  (pos? (get-in metainfo ["info" "length"] 0)))

(defn multi-file-torrent?
  "Returns whether metainfo represents a multi-file torrent."
  [metainfo]
  (not (single-file-torrent? metainfo)))

(defn private-torrent?
  "Returns whether metainfo represents a private torrent."
  [metainfo]
  (not= 0 (get-in metainfo ["info" "private"] 0)))

(defn public-torrent?
  "Returns whether metainfo represents a public torrent."
  [metainfo]
  (not (private-torrent? metainfo)))

(defn torrent-name
  "Returns the torrent name."
  [metainfo]
  (get-in metainfo ["info" "name"]))

(defn torrent-announce
  "Returns the torrent tracker URL."
  [metainfo]
  (get metainfo "announce"))

(defn torrent-announce-list
  "Returns the list of torrent tracker URLs."
  [metainfo]
  (get metainfo "announce-list"))

(defn torrent-piece-length
  "Returns the length of each piece, in bytes."
  [metainfo]
  (get-in metainfo ["info" "piece length"]))

(defn torrent-pieces-hash
  "Returns the torrent pieces hash."
  [metainfo]
  (get-in metainfo ["info" "pieces"]))

(defn torrent-pieces-hash-list
  "Returns a lazy seq containing the hash for each piece."
  [metainfo]
  (map hex-from-bytes
   (partition 20 (torrent-pieces-hash metainfo))))

(defn torrent-pieces-count
  "Returns the number of pieces."
  [metainfo]
  (/ (count (torrent-pieces-hash metainfo)) 20))

(defn torrent-size
  "Return the total size of the torrent, in bytes."
  [metainfo]
  (or (get-in metainfo ["info" "length"])
      (reduce + (map #(get % "length")
                     (get-in metainfo ["info" "files"])))))

(defn torrent-info-hash
  "Returns the byte array of the torrent info hash."
  [metainfo]
  (let [enc (bencode (get metainfo "info") {:raw-str? true})
        dig (MessageDigest/getInstance "SHA1")]
    (.digest dig enc)))

(defn torrent-info-hash-str
  "Returns the torrent info hash in a readable format."
  [metainfo]
  (hex-from-bytes (torrent-info-hash metainfo)))

(defn torrent-creation-date
  "Returns an instance of java.util.Date representing the torrent
   creation date."
  [metainfo]
  (if-let [^int timestamp (get metainfo "creation date")]
    (Date. (* 1000 timestamp))))

(defn torrent-files
  "Returns the torrent list of files."
  [metainfo]
  (get-in metainfo ["info" "files"]))

(defn torrent-created-by
  "Returns the torrent maker name."
  [metainfo]
  (get metainfo "created by"))

(defn torrent-comment
  "Returns the torrent comment."
  [metainfo]
  (get metainfo "comment"))

(defn torrent-magnet-link
  "Returns the torrent magnet link."
  [metainfo]
  (str "magnet:?xt=urn:btih:" (torrent-info-hash-str metainfo)
       "&dn=" (URLEncoder/encode (torrent-name metainfo) "UTF-8")
       (reduce #(str %1 "&tr=" (URLEncoder/encode %2 "UTF-8"))
               "" (flatten (torrent-announce-list metainfo)))))
