(ns bencode.type.stream
  (:use [bencode.protocol]
        [bencode.utils]))

(extend-protocol Bdecodable
  java.io.InputStream
    (bdecode [self opts]
      (let [out (bdecode-type! self opts)]
        (if (= -1 (.read self))
          out
          (error "Unexpected trailing data")))))
