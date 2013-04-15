(ns bencode.core
  (:require [bencode.protocol :as protocol])
  (:use [bencode.type.number]
        [bencode.type.string]
        [bencode.type.list]
        [bencode.type.dict]
        [bencode.type.stream]))

(defn bencode
  "Bencodes the given object."
  ([obj]
     (bencode obj nil))
  ([obj opts]
     (let [out (or (get opts :to (java.io.ByteArrayOutputStream.)))]
       (protocol/bencode! obj out opts)
       (when-not (:to opts)
         (let [arr (.toByteArray out)]
           (if (:raw-str? opts)
             arr
             (String. arr "UTF-8")))))))

(defn bdecode
  "Bdecodes the given input."
  ([in]
     (protocol/bdecode in nil))
  ([in opts]
     (protocol/bdecode in opts)))
