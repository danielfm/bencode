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

(defn torrent-piece-length
  "Returns the length of each piece, in bytes."
  [meta-info]
  (get-in meta-info ["info" "piece length"]))

(defn torrent-pieces-hash
  "Returns the torrent pieces hash."
  [meta-info]
  (get-in meta-info ["info" "pieces"]))

(defn torrent-pieces-count
  "Returns the number of pieces."
  [meta-info]
  (/ (count (torrent-pieces-hash meta-info)) 20))

(defn torrent-size
  "Return the total size of the torrent, in bytes."
  [meta-info]
  (or (get-in meta-info ["info" "length"])
      (reduce + (map #(get % "length")
                     (get-in meta-info ["info" "files"])))))

(defn torrent-info-hash
  "Returns the torrent info hash."
  [meta-info]
  (let [enc (bencode (get meta-info "info") {:raw-str? true})
        dig (MessageDigest/getInstance "SHA1")
        res (.digest dig enc)]
    (hex-from-bytes res)))
