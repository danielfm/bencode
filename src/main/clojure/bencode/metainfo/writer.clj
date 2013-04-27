(ns bencode.metainfo.writer
  (:use [bencode.core]
        [bencode.utils])
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.io File]
           [java.nio ByteBuffer]
           [java.security MessageDigest]
           [java.util.concurrent TimeUnit]))

(defn file-path
  "Returns the file path relative to directory dir."
  [^File dir ^File file]
  (let [sep File/separator
        dir-path (str (.getCanonicalPath dir) sep) 
        file-path (.getCanonicalPath file)]
    (str/split (str/replace file-path dir-path "") (re-pattern sep))))

(defn file-entry
  "Returns a dictionary that represents a torrent file entry."
  [^File dir ^File file]
  {"length" (.length file) "path" (file-path dir file)})

(defn scan-files
  "Returns a seq containing all files inside directory dir."
  [^File dir]
  (filter #(.isFile ^File %) (file-seq dir)))

(defn torrent-files
  "Returns a seq containing all file entries from the files inside dir."
  [^File dir files]
  (let [path (.getCanonicalPath dir)]
    (map #(file-entry dir %) files)))

(defn torrent-piece-length
  "Returns the size, in bytes, for a piece length of 2^n KB."
  [n]
  (let [x (or n 8)]
    (int (* 1024 (Math/pow 2 x)))))

(defn torrent-main-announce
  "Returns the first URL from the first announce group."
  [announce-list]
  (first (first announce-list)))

(defn- assoc-pieces-hash
  "Computes the hash for each piece in parallel and appends them to metainfo
   dictionary."
  [files metainfo n-threads]
  (let [piece-length (get-in metainfo ["info" "piece length"])
        digest (bencode.ParallelPieceDigest. files piece-length)]
    (assoc-in metainfo ["info" "pieces"] (.computeHash digest n-threads))))

(defn- single-file-metainfo
  "Returns a metainfo dictionary for single-file torrent."
  [^File file base-metainfo n-threads]
  (assoc-pieces-hash [file]
                     (assoc-in base-metainfo ["info" "length"]
                               (.length file))
                     n-threads))

(defn- multi-file-metainfo
  "Returns a metainfo dictionary for multi-file torrent."
  [^File dir files base-metainfo n-threads]
  (assoc-pieces-hash files
                     (assoc-in base-metainfo ["info" "files"]
                               (torrent-files dir files))
                     n-threads))

(defn create-metainfo
  "Creates a BitTorrent metainfo dictionary."
  [& {:keys [^File file announce-list name comment created-by
             piece-length-power private? n-threads]}]
  (let [now           (System/currentTimeMillis)
        n-thread      (or n-threads 2)
        files         (scan-files file)
        created-at    (.toSeconds TimeUnit/MILLISECONDS now)
        created-by    (or created-by "Bencode")
        torrent-name  (or name (.getName file))
        private-flag  (flag-from-bool private?)
        comment       (or comment "")
        main-announce (torrent-main-announce announce-list)
        piece-length  (torrent-piece-length piece-length-power)
        base-metainfo {"info" {"name"         torrent-name
                               "private"      private-flag
                               "piece length" piece-length}
                       "created by"    created-by
                       "creation date" created-at
                       "comment"       comment
                       "announce"      main-announce
                       "announce-list" announce-list}]
    (if (.isFile file)
      (single-file-metainfo file base-metainfo n-threads)
      (multi-file-metainfo file files base-metainfo n-threads))))
