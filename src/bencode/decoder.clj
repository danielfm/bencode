(ns bencode.decoder
  (:require [clojure [edn :as edn]])
  (:use [bencode.error]))

(defn- digit?
  "Returns whether ch is a digit, e.g., value between 0 and 9."
  [ch]
  (let [i (int ch)]
    (and (>= i 48) (<= i 57))))

(defmulti bdecode-type
  "Dispatches to the proper decoder method according to the first element
of seq."
  (fn [seq _]
    (let [f (first seq)]
      (cond
       (= f \i)   ::int
       (= f \l)   ::seq
       (= f \d)   ::dict
       (digit? f) ::str
       :else      ::unknown))))

(defn- read-digits
  "Reads from seq until a non-digit char is found, and returns an array
with the number's digits at index 0 and the remaining of seq at index 1."
  [seq]
  (loop [data [] rem seq]
    (let [f (first rem)]
      (if (digit? f)
        (recur (conj data f) (rest rem))
        [(apply str data) rem]))))

(defn- invalid-number?
  "Returns whether digits reprents an invalid number according to the spec."
  [digits]
  (or (empty? digits)
      (and (> (count digits) 1) (= \0 (first digits)))))

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
  (let [[size seq] (read-digits seq)
        len (edn/read-string size)
        text (apply str (take len (rest seq)))]
    (if (> len (count text))
      (error "Unexpected end of string")
      [text (drop (inc len) seq)])))

(defmethod bdecode-type ::int [seq opts]
  (let [number-seq (rest seq)
        sign (#{\- \+} (first number-seq))]
    (if (and (= \- sign) (= \0 (second number-seq)))
      (error "Invalid number expression")
      (let [[digits rem] (read-digits (if sign (rest number-seq) number-seq))]
        (if (or (invalid-number? digits) (not (= \e (first rem))))
          (error "Invalid number expression")
          [(edn/read-string (str sign digits)) (rest rem)])))))

(defmethod bdecode-type ::seq [seq opts]
  (loop [data [] rem (rest seq)]
    (if (= \e (first rem))
      [data (rest rem)]
      (let [[item rem] (bdecode-type rem opts)]
        (recur (conj data item) rem)))))

(defmethod bdecode-type ::dict [seq opts]
  (loop [data (sorted-map) rem (rest seq)]
    (if (= \e (first rem))
      [data (rest rem)]
      (let [[key val rem] (bdecode-dict-entry rem opts)]
        (if (string? key)
          (recur (assoc data (dict-key key opts) val) rem)
          (error "Only strings should be used as dictionary keys"))))))

(defn bdecode
  "Bdecodes the given string."
  [s opts]
  (let [[data rem] (bdecode-type s opts)]
    (if (empty? rem)
      data
      (error "Unexpected trailing data"))))
