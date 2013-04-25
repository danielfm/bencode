(ns bencode.utils)

(defn flag-from-bool
  "Returns 1 if b is true, false otherwise."
  [b]
  (if b 1 0))

(defn digit?
  "Returns whether byte b is a digit, e.g., value between 0 and 9."
  [b]
  (and (>= b 48) (<= b 57)))

(defn read-digits!
  "Reads from input stream in until a non-digit char is found and returns
   a string containing the digits read."
  [in]
  (loop [data []]
    (.mark in 1)
    (let [f (.read in)]
      (if (digit? f)
        (recur (conj data (char f)))
        (do
          (.reset in)
          (apply str data))))))

(defn error
  "Raises an exception that signals an unexpected condition during
   encoding/decoding process."
  [msg]
  (throw (IllegalArgumentException. msg)))
