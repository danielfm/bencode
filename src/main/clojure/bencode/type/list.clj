(ns bencode.type.list
  (:use [bencode.protocol]
        [bencode.utils])
  (:import [java.util Set]
           [java.io InputStream OutputStream]))

(defn- bencode-seq!
  "Bencodes the given sequence."
  [seq ^OutputStream out opts]
  (.write out (int \l))
  (doall (map #(bencode! % out opts) seq))
  (.write out (int \e)))

(extend-protocol Bencodable
  java.util.Set
    (bencode! [self out opts]
      (bencode-seq! self out opts))

  java.util.List
    (bencode! [self out opts]
      (bencode-seq! self out opts)))

(defmethod bdecode-type! :list [^InputStream in opts]
  (loop [data []]
    (.mark in 1)
    (if (= \e (char (.read in)))
      data
      (do
        (.reset in)
        (let [item (bdecode-type! in opts)]
          (recur (conj data item)))))))
