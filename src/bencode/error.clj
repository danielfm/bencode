(ns bencode.error)

(defn error
  "Raises an exception that signals an unexpected condition during
encoding/decoding process."
  [msg]
  (throw (IllegalArgumentException. msg)))
