(ns bencode.type.dict
  (:use [bencode.protocol]
        [bencode.utils])
  (:import [java.util Map]
           [java.io InputStream OutputStream]))

(defn- valid-dict?
  "Returns whether the dictionary m only contains valid keys."
  [m]
  (every? (some-fn keyword? string?)
          (keys m)))

(defn- sort-dict
  "Returns a normalized and sorted version of dictionary m."
  [m]
  (into (sorted-map)
        (map (fn [[k v]]
               [(name k) v])
             m)))

(defn- dict-key
  "Returns the final dictionary key according to the options defined in opts."
  [key opts]
  (if (:str-keys? opts)
    key
    (keyword key)))

(defn- bdecode-dict-entry!
  "Bdecodes a dictionary entry from input stream in, where the key and its
   corresponding value comes in a row."
  [in opts]
  (let [raw-keys (:raw-keys opts)
        key (bdecode-type! in opts)
        val (bdecode-type! in (if (some #{key} raw-keys)
                                (assoc opts :raw-str? true)
                                opts))]
    [key val]))

(extend-protocol Bencodable
  Map
    (bencode! [self ^OutputStream out opts]
      (when-not (valid-dict? self)
        (error "Only keywords can be used as dictionary keys"))
      (.write out (int \d))
      (doall
       (map (fn [[key val]]
              (bencode! key out opts)
              (bencode! val out opts))
            (sort-dict self)))
      (.write out (int \e))))

(defmethod bdecode-type! :dict [^InputStream in opts]
  (loop [data (sorted-map)]
    (.mark in 1)
    (if (= \e (char (.read in)))
      data
      (do
        (.reset in)
        (let [[key val] (bdecode-dict-entry! in opts)]
          (if (string? key)
            (recur (assoc data (dict-key key opts) val))
            (error "Only strings should be used as dictionary keys")))))))
