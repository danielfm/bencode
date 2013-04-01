(ns bencode.decoder
  (:require [clojure [edn :as edn]])
  (:use [bencode.error]))

(defn- digit?
  "Returns whether byte b is a digit, e.g., value between 0 and 9."
  [b]
  (and (>= b 48) (<= b 57)))

(defmulti bdecode-type
  "Dispatches to the proper decoder method according to the first element
of seq."
  (fn [seq _]
    (let [b (first seq)
          ch (char b)]
      (cond
       (= ch \i)   ::int
       (= ch \l)   ::seq
       (= ch \d)   ::dict
       (digit? b) ::str
       :else      ::unknown))))

(defn- read-digits
  "Reads from seq until a non-digit char is found, and returns an array
with the number's digits at index 0 and the remaining of seq at index 1."
  [seq]
  (loop [data [] rem seq]
    (let [f (first rem)]
      (if (digit? f)
        (recur (conj data (char f)) (rest rem))
        [(apply str data) rem]))))

(defn- invalid-number?
  "Returns whether digits represents an invalid number according to the spec."
  [digits]
  (or (empty? digits)
      (and (> (count digits) 1)
           (= \0 (char (first digits))))))

(defn- bdecode-dict-entry
  "Bdecodes a dictionary entry from sequence seq, where the key and its
corresponding value are concatenated."
  [seq opts]
  (let [[key rem] (bdecode-type seq opts)
        [val rem] (bdecode-type rem opts)]
    [key val rem]))

(defn- dict-key
  "Returns the final dictionary key according to the options defined in
opts."
  [key opts]
  (if (:str-keys? opts)
    key
    (keyword key)))

(defmethod bdecode-type ::unknown [seq opts]
  (error "Unexpected token"))

(defmethod bdecode-type ::str [seq opts]
  (let [[size rem] (read-digits seq)
        len (edn/read-string size)
        data (byte-array len)]
    (loop [i 0 rem (rest rem)]
      (when (< i len)
        (when (empty? rem)
          (error "Unexpected end of string"))
        (aset-byte data i (first rem))
        (recur (inc i) (rest rem))))
    [(String. data "UTF-8") (drop (inc len) rem)]))

(defmethod bdecode-type ::int [seq opts]
  (let [number-seq (rest seq)
        sign (#{\- \+} (char (first number-seq)))]
    (if (and (= \- sign)
             (= \0 (char (second number-seq))))
      (error "Invalid number expression")
      (let [[digits rem] (read-digits (if sign (rest number-seq) number-seq))]
        (if (or (invalid-number? digits)
                (not (= \e (char (first rem)))))
          (error "Invalid number expression")
          [(edn/read-string (str sign digits)) (rest rem)])))))

(defmethod bdecode-type ::seq [seq opts]
  (loop [data [] rem (rest seq)]
    (if (= \e (char (first rem)))
      [data (rest rem)]
      (let [[item rem] (bdecode-type rem opts)]
        (recur (conj data item) rem)))))

(defmethod bdecode-type ::dict [seq opts]
  (loop [data (sorted-map)
         rem (rest seq)]
    (if (= \e (char (first rem)))
      [data (rest rem)]
      (let [[key val rem] (bdecode-dict-entry rem opts)]
        (if (string? key)
          (recur (assoc data (dict-key key opts) val) rem)
          (error "Only strings should be used as dictionary keys"))))))

(defn bdecode
  "Bdecodes the given string."
  [s opts]
  (let [bytes (if (string? s) (.getBytes s) s)
        [data rem] (bdecode-type bytes opts)]
    (if (empty? rem)
      data
      (error "Unexpected trailing data"))))
