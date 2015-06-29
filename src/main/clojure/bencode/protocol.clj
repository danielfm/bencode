(ns bencode.protocol
  (:use [bencode.utils])
  (:import [java.io InputStream]))

(defprotocol Bdecodable
  "All bdecodable data types must implement this protocol."
  (bdecode [self opts] "Returns the data structure from this bencoded object."))

(defprotocol Bencodable
  "All bencodable data types must implement this protocol."
  (bencode! [self out opts] "Returns a bencoded string of self."))

(defmulti bdecode-type!
  "Dispatches to the proper decoder method according to the next byte read
   from input stream in."
  (fn [^InputStream in _]
    (.mark in 1)
    (let [b (.read in)
          ch (char b)]
      (cond
       (= ch \i)  :number
       (= ch \l)  :list
       (= ch \d)  :dict
       (digit? b) (do (.reset in) :string)
       :else      (do (.reset in) :unknown)))))

(defmethod bdecode-type! :unknown [in opts]
  (error (str "Unexpected token: " (String. (.read in)))))
