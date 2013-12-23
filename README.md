# bencode

Clojure implementation of [Bencode](http://bittorrent.org/beps/bep_0003.html#bencoding),
the encoding used by BitTorrent for storing and transmitting loosely structured data.

## Features

* Parsing bencode strings directly to Clojure data structures and vice-versa
* Support for input and output streams
* Read and write BitTorrent metainfo (.torrent) files
* Can generate Magnet link from BitTorrent metainfo
* Multi-threaded algorithm for fast piece hashing


## Installation

Add the following dependency to your _project.clj_ file:

````clojure

[bencode "0.2.4"]
````


## Usage

### Encoding and Decoding

First, import the `bencode.core` namespace:

````clojure

(use '[bencode.core])
````

At this point, you should be able to use `bencode` and `bdecode` functions for
encoding and decoding, respectively:

````clojure

(bencode {:cow "moo" :spam ["info" 32]})
;; -> "d3:cow3:moo4:spaml4:infoi32eee"

(bdecode "d3:cow3:moo4:spaml4:infoi32eee")
;; -> {:cow "moo", :spam ["info" 32]}
```


##### Supported Data Types

According to the Bencoding spec, only _strings_, _integers_, _lists_ and
_dictionaries_ should be supported. Furthermore, only strings can be used as
keys in a dictionary, and the keys must appear in sorted order (sorted as raw
strings, not alphanumerics).

On the Clojure side, _keywords_ are encoded as _strings_, _sets_ and _vectors_
are encoded as _lists_, and all integers - _byte_, _short_, _int_, _long_,
_big integers_ - are encoded as _integers_ with arbitrary size, and decoded to
the smallest type which can hold the number without losing data.


#### Encoding Options

The `bencode` function also accepts an optional map:

````clojure

(bencode "moo" {:raw-str? true})
;; -> #<byte[] [B@53c059f6>
````

These are the supported encoding options:

* `:to` - Instance of `OutputStream` where the encoding result should be
  written to. Default: `nil`
* `:raw-str?` - Whether the string being encoded should be returned as a
  byte array. This option can only be used if the option `:to` is absent.
  Default: `false`


#### Decoding Options

The `bdecode` function also accepts an optional map:

````clojure

(bdecode "d3:cow3:moo4:spaml4:infoi32eee", {:str-keys? true :raw-keys ["spam"]})
;; -> {"cow" "moo", "spam" [#<byte[] [B@74184b3b> 32]}
````

The input might be either a _string_, a _byte array_ or an _input stream_.

These are the supported decoding options:

* `:str-keys?` - Whether strings should be used as dictionary keys instead of
  keywords. Default: `false`
* `:raw-keys` - List containing all dictionary keys whose values should be
  decoded as raw strings instead of UTF-8-encoded strings. Default: `nil`


### BitTorrent Metainfo

#### Reading Metainfo Files

A collection of useful functions for BitTorrent metainfo parsing are available
under the `bencode.metainfo.reader` namespace:

````clojure

(use '[bencode.metainfo.reader])

;; parsing an input stream
(def metainfo (parse-metainfo input-stream))

;; parsing a file given its path
(def metainfo (parse-metainfo-file "/file/path"))
````

To extract bits of information from this metainfo:

````clojure

(torrent-name metainfo)
;; -> "my.supercool.torrent"

(public-torrent? metainfo)
;; -> true

(torrent-info-hash-str metainfo)
;; -> "b174c9c090275f858853ba5ea1b01762eaa59f9d"
````

It's also possible to generate the Magnet link for a metainfo:

````clojure

(torrent-magnet-link metainfo)
;; -> "magnet:?xt=urn:btih:..."
````

Please check out the source code for a complete list of the available functions.

#### Creating a Metainfo Dictionary

It's very easy to create a metainfo file:

````clojure

(use '[bencode.metainfo.writer])

(def metainfo (create-metainfo :file               file-obj
                               :created-by         "You"
                               :announce-list      [["tracker-1"] ["tracker-2"]]
                               :private?           false
                               :name               "optional.torrent.name"
                               :piece-length-power 7 ;; piece length = 2^7KiB
                               :n-threads          4 ;; for fast parallel piece hashing
                               :comment            "Some torrent"))
````

This operation might take several minutes depending on the file size.

To be able to import this file in your favorite BitTorrent client, just bencode
`metainfo` to a _.torrent_ file and you're done:


````clojure

(bencode metainfo {:to file-out-stream})
````


## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for further details.
