(ns bencode.encoder
  (use [bencode.error]))

(defprotocol Bencodable
  "All bencodable data types must implement this protocol."
  (bencode [self] "Returns a bencoded string of self."))

(defn- bencode-number
  "Bencodes the given number, which is assumed to be an integer."
  [n]
  (str "i" n "e"))

(defn- bencode-seq
  "Bencodes the given sequence."
  [seq]
  (let [encoded-items (map bencode seq)]
      (str "l" (reduce str encoded-items) "e")))

(defn- bencode-dict-entry
  "Returns an encoded dictionary entry."
  [[key val]]
  (str (bencode key) (bencode val)))

(defn- valid-dict?
  "Returns whether the dictionary m only contains valid keys."
  [m]
  (every? (some-fn keyword? string?) (keys m)))

(defn- sort-dict
  "Returns a normalized and sorted version of dictionary m."
  [m]
  (into (sorted-map)
        (map (fn [[k v]] [(name k) v]) m)))

(extend-protocol Bencodable
  CharSequence
  (bencode [self]
    (str (.length self) ":" self))

  clojure.lang.Keyword
  (bencode [self]
    (bencode (name self)))

  Byte
  (bencode [self]
    (bencode-number self))

  Short
  (bencode [self]
    (bencode-number self))

  Integer
  (bencode [self]
    (bencode-number self))

  Long
  (bencode [self]
    (bencode-number self))

  BigInteger
  (bencode [self]
    (bencode-number self))

  clojure.lang.BigInt
  (bencode [self]
    (bencode-number self))

  java.util.Set
  (bencode [self]
    (bencode-seq self))

  java.util.List
  (bencode [self]
    (bencode-seq self))

  java.util.Map
  (bencode [self]
    (if (valid-dict? self)
      (let [encoded-entries (map bencode-dict-entry (sort-dict self))]
        (str "d" (reduce str encoded-entries) "e"))
      (error "Only keywords can be used as dictionary keys"))))
