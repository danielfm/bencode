(ns bencode.main
  (:gen-class)
  (:use [bencode.core]
        [bencode.metainfo.reader])
  (:require [clojure.java [io :as io]])
  (:import  [java.text SimpleDateFormat]))

(defn format-date [torrent]
  (let [f (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSZ")]
    (.format f (torrent-creation-date torrent))))

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
          (println "Torrent Name..: " (torrent-name torrent))
          (println "Creation Date.: " (format-date torrent))
          (println "Created By....: " (torrent-created-by torrent))
          (println "Info Hash.....: " (torrent-info-hash torrent))
          (println "Private.......: " (private-torrent? torrent))
          (println "Comment.......: " (torrent-comment torrent))

          (println "")

          (println "Piece Length..: " (str (torrent-piece-length torrent) " bytes"))
          (println "# of Pieces...: " (torrent-pieces-count torrent))
          (println "Total Size....: " (str (torrent-size torrent) " bytes"))

          (println "")

          (println "Trackers......: " (torrent-announce torrent))
          (doall
           (map-indexed print-announce-group (torrent-announce-list torrent)))

          (println "")

          (when-let [files (get-in torrent ["info" "files"])]
            (println "Files.........: " (count files))
            (doall (map-indexed print-file files))
            (println ""))))))
