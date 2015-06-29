(ns bencode.type.string
  (:require [clojure.edn :as edn])
  (:use [bencode.protocol]
        [bencode.utils])
  (:import [java.io InputStream OutputStream ByteArrayInputStream]))

(extend-protocol Bdecodable
  String
    (bdecode [self {:keys [^String encoding] :or {encoding "UTF-8"} :as opts}]
      (bdecode (.getBytes self encoding) opts)))

(extend-type (Class/forName "[B")
  Bdecodable
    (bdecode [self opts]
      (let [stream (ByteArrayInputStream. self)]
        (bdecode stream opts))))

(extend-protocol Bencodable
  String
    (bencode! [self out {:keys [^String encoding] :or {encoding "UTF-8"} :as opts}]
      (bencode! (.getBytes self encoding) out opts))

  clojure.lang.Keyword
    (bencode! [self out opts]
      (bencode! (name self) out opts)))

(extend-type (Class/forName "[B")
  Bencodable
    (bencode! [^String self ^OutputStream out opts]
      (let [^String len (str (count self))]
        (.write out (.getBytes len) 0 (count len))
        (.write out (int \:))
        (.write out self 0 (count self)))))

(defmethod bdecode-type! :string [^InputStream in {:keys [^String encoding] :or {encoding "UTF-8"} :as opts}]
  (let [^String size (read-digits! in)
        ^String len(edn/read-string size)
        ^bytes data (byte-array len)]
    (when-not (= \: (char (.read in)))
      (error "Expected ':'"))
    (if (and (> len 0) (< (.read in data 0 len) len))
      (error "Unterminated string")
      (if (:raw-str? opts)
        data
        (String. data encoding)))))
