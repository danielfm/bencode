(ns bencode.type.number
  (:require [clojure.edn :as edn])
  (:use [bencode.protocol]
        [bencode.utils]))

(defn- bencode-number!
  "Bencodes the given number, which is assumed to be an integer."
  [n out]
  (let [s (str "i" n "e")]
    (.write out (.getBytes s) 0 (count s))))

(defn- invalid-number?
  "Returns whether digits represents an invalid number according to the spec."
  [digits]
  (or (empty? digits)
      (and (> (count digits) 1)
           (= \0 (char (first digits))))))

(extend-protocol Bencodable
  Byte
    (bencode! [self out opts]
      (bencode-number! self out))

  Short
    (bencode! [self out opts]
      (bencode-number! self out))

  Integer
    (bencode! [self out opts]
      (bencode-number! self out))

  Long
    (bencode! [self out opts]
      (bencode-number! self out))

  BigInteger
    (bencode! [self out opts]
      (bencode-number! self out))

  clojure.lang.BigInt
    (bencode! [self out opts]
      (bencode-number! self out)))

(defmethod bdecode-type! :number [in opts]
  (.mark in 1)
  (let [f (char (.read in))
        sign (#{\- \+} f)]
    (when-not sign
      (.reset in))
    (.mark in 1)
    (let [s (char (.read in))]
      (if (and (= \- sign) (= \0 s))
        (error "Invalid number expression")
        (do
          (.reset in)
          (let [digits (read-digits! in)]
            (.reset in)
            (if (or (invalid-number? digits)
                    (not (= \e (char (.read in)))))
              (error "Invalid number expression")
              (edn/read-string (str sign digits)))))))))
