# bencode

Clojure implementation of [Bencode](http://bittorrent.org/beps/bep_0003.html#bencoding),
the encoding used by BitTorrent for storing and transmitting loosely structured data.


## Installation

Add the following dependency to your _project.clj_ file:

````clojure

[bencode "0.0.1"]
````


## Usage

Just use the `bencode.core` namespace and use `bencode` and `bdecode` for
encoding and decoding, respectively:

````clojure

user=> (use '[bencode.core])
nil
user=> (bencode {:cow "moo" :spam ["info" 32]})
"d3:cow3:moo4:spaml4:infoi32eee"
user=> (bdecode "d3:cow3:moo4:spaml4:infoi32eee")
{:cow "moo", :spam ["info" 32]}
```

## License

Copyright (C) Daniel Fernandes Martins

Distributed under the New BSD License. See COPYING for further details.
