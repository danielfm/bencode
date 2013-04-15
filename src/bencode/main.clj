(ns bencode.main
  (:gen-class)
  (:use [bencode.core]
        [bencode.metainfo])
  (:require [clojure.java [io :as io]])
  (:import [java.util Date]
           [java.text SimpleDateFormat]))

(defn format-date [timestamp]
  (let [f (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSZ")]
    (.format f (Date. (* timestamp 1000)))))

(defn print-announce-group [idx group]
  (print (str (inc idx) ". "))
  (apply print (interpose ", " group))
  (println ""))

(defn print-file [idx file]
  (print (str (inc idx) ". "))
  (print (apply str (interpose "/" (get file "path")))
         (str "[" (get file "length") " bytes]"))
  (println ""))

(defn -main [& args]
  (let [f (io/file (nth args 0))]
    (with-open [stream (io/input-stream f)]
      (let [torrent (parse-metainfo stream)]
          (println "Torrent Name..: " (get-in torrent ["info" "name"]))
          (println "Creation Date.: " (format-date (get torrent "creation date")))
          (println "Created By....: " (get torrent "created by"))
          (println "Info Hash.....: " (torrent-info-hash torrent))
          (println "Private.......: " (get-in torrent ["info" "private"] 0))
          (println "Comment.......: " (get torrent "comment"))

          (println "")

          (println "Piece Length..: " (str (torrent-piece-length torrent) " bytes"))
          (println "# of Pieces...: " (torrent-pieces-count torrent))
          (println "Total Size....: " (str (torrent-size torrent) " bytes"))

          (println "")

          (println "Trackers......: " (get torrent "announce"))
          (doall
           (map-indexed print-announce-group (get torrent "announce-list")))

          (println "")

          (when-let [files (get-in torrent ["info" "files"])]
            (println "Files.........: " (count files))
            (doall (map-indexed print-file files))
            (println ""))))))
