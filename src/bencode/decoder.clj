(ns bencode.decoder
  (:use [bencode.error]))

(defn- digit?
  "Returns whether ch is a digit, e.g., value between 0 and 9."
  [ch]
  (let [i (int ch)]
    (and (>= i 48) (<= i 57))))

(defn- read-digits
  "Reads from seq until a non-digit char is found, and returns an array
with the number's digits at index 0 and the remaining of seq at index 1."
  [seq]
  (loop [data [] rem seq]
    (let [f (first rem)]
      (if (digit? f)
        (recur (conj data f) (rest rem))
        [(apply str data) rem]))))

(defmulti bdecode-type
  "Dispatches to the proper decoder method according to the first element
of seq."
  (fn [seq]
    (let [f (first seq)]
      (cond
       (= f \i)   ::int
       (= f \l)   ::seq
       (= f \d)   ::dict
       (digit? f) ::str
       :else      ::unknown))))

(defmethod bdecode-type ::unknown [seq]
  (error "Unexpected token"))

(defmethod bdecode-type ::str [seq]
  (let [[size seq] (read-digits seq)
        len (read-string size)
        text (apply str (take len (rest seq)))]
    (if (> len (count text))
      (error "Unexpected end of string")
      [text (drop (inc len) seq)])))

(defmethod bdecode-type ::int [seq]
  (let [number-seq (rest seq)
        sign (#{\- \+} (first number-seq))]
    (if (and (= \- sign) (= \0 (second number-seq)))
      (error "Invalid number expression")
      (let [[digits rem] (read-digits (if sign (rest number-seq) number-seq))]
        (if (or (empty? digits) (not (= \e (first rem))))
          (error "Invalid number expression")
          [(read-string (str sign digits)) (rest rem)])))))

(defmethod bdecode-type ::seq [seq]
  (loop [data [] rem (rest seq)]
    (if (= \e (first rem))
      [data (rest rem)]
      (let [[item rem] (bdecode-type rem)]
        (recur (conj data item) rem)))))

(defmethod bdecode-type ::dict [seq]
  (loop [data (sorted-map) rem (rest seq)]
    (if (= \e (first rem))
      [data (rest rem)]
      (let [[key rem] (bdecode-type rem)
            [val rem] (bdecode-type rem)]
        (if (string? key)
          (recur (assoc data (keyword key) val) rem)
          (error "Only strings should be used as dictionary keys"))))))

(defn bdecode
  "Bdecodes the given string."
  [s]
  (let [[data rem] (bdecode-type s)]
    (if (empty? rem)
      data
      (error "Unexpected trailing data"))))
