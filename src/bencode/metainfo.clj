(ns bencode.metainfo
  (:use [bencode.core])
  (:import [java.security MessageDigest]))

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

(defn single-file-torrent?
  "Returns whether metainfo represents a single-file torrent."
  [metainfo]
  (pos? (get-in metainfo ["info" "length"] 0)))

(defn multi-file-torrent?
  "Returns whether metainfo represents a multi-file torrent."
  [metainfo]
  (not (single-file-torrent? metainfo)))

(defn torrent-piece-length
  "Returns the length of each piece, in bytes."
  [metainfo]
  (get-in metainfo ["info" "piece length"]))

(defn torrent-pieces-hash
  "Returns the torrent pieces hash."
  [metainfo]
  (get-in metainfo ["info" "pieces"]))

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
  "Returns the torrent info hash."
  [metainfo]
  (let [enc (bencode (get metainfo "info") {:raw-str? true})
        dig (MessageDigest/getInstance "SHA1")
        res (.digest dig enc)]
    (hex-from-bytes res)))
