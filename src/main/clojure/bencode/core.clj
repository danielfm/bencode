(ns bencode.core
  (:require [bencode.protocol :as protocol])
  (:use [bencode.type.number]
        [bencode.type.string]
        [bencode.type.list]
        [bencode.type.dict]
        [bencode.type.stream])
  (:import [java.io ByteArrayOutputStream]))

(defn bencode
  "Bencodes the given object."
  ([obj]
     (bencode obj nil))
  ([obj {:keys [encoding] :or {encoding "UTF-8"} :as opts}]
     (let [^ByteArrayOutputStream out (or (get opts :to (ByteArrayOutputStream.)))]
       (protocol/bencode! obj out (assoc opts :encoding encoding))
       (when-not (:to opts)
         (let [^bytes arr (.toByteArray out)]
           (if (:raw-str? opts)
             arr
             (String. arr encoding)))))))

(defn bdecode
  "Bdecodes the given input."
  ([in]
     (protocol/bdecode in nil))
  ([in {:keys [encoding] :or {encoding "UTF-8"} :as opts}]
     (protocol/bdecode in (assoc opts :encoding encoding))))
