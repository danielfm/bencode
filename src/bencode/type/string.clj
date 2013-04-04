(ns bencode.type.string
  (:require [clojure.edn :as edn])
  (:use [bencode.protocol]
        [bencode.utils]))

(extend-protocol Bdecodable
  String
    (bdecode [self opts]
      (bdecode (.getBytes self "UTF-8") opts)))

(extend-type (Class/forName "[B")
  Bdecodable
    (bdecode [self opts]
      (let [stream (java.io.ByteArrayInputStream. self)]
        (bdecode stream opts))))

(extend-protocol Bencodable
  String
    (bencode! [self out opts]
      (bencode! (.getBytes self "UTF-8") out opts))

  clojure.lang.Keyword
    (bencode! [self out opts]
      (bencode! (name self) out opts)))

(extend-type (Class/forName "[B")
  Bencodable
    (bencode! [self out opts]
      (let [len (str (count self))]
        (.write out (.getBytes len) 0 (count len))
        (.write out (int \:))
        (.write out self 0 (count self)))))

(defmethod bdecode-type! :string [in opts]
  (let [size (read-digits! in)
        len (edn/read-string size)
        data (byte-array len)]
    (when-not (= \: (char (.read in)))
      (error "Expected ':'"))
    (if (and (> len 0) (< (.read in data 0 len) len))
      (error "Unterminated string")
      (if (:raw-str? opts)
        data
        (String. data)))))
