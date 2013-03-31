(ns bencode.core
  (:require (bencode encoder decoder)))

(defn bencode
  "Bencodes the given object."
  [obj]
  (bencode.encoder/bencode obj))

(defn bdecode
  "Bdecodes the given string."
  [s]
  (bencode.decoder/bdecode s))
