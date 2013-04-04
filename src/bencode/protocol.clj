(ns bencode.protocol
  (:use [bencode.utils]))

(defprotocol Bdecodable
  "All bdecodable data types must implement this protocol."
  (bdecode [self opts] "Returns the data structure from this bencoded object."))

(defprotocol Bencodable
  "All bencodable data types must implement this protocol."
  (bencode! [self out opts] "Returns a bencoded string of self."))

(defmulti bdecode-type!
  "Dispatches to the proper decoder method according to the first element
   of seq."
  (fn [in _]
    (.mark in 1)
    (let [b (.read in)
          ch (char b)]
      (cond
       (= ch \i)  :number
       (= ch \l)  :list
       (= ch \d)  :dict
       (digit? b) (do (.reset in) :string)
       :else      :unknown))))

(defmethod bdecode-type! :unknown [seq opts]
  (error "Unexpected token"))
