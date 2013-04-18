(defproject bencode "0.1.0"
  :description "BitTorrent encoding implementation for Clojure."
  :url "https://github.com/danielfm/bencode"
  :license {:name "BSD License"
            :url "http://raw.github.com/danielfm/bencode/master/COPYING"}
  :dependencies [[org.clojure/clojure "1.5.0"]]
  :main bencode.main
  :jvm-opts ["-Dfile.encoding=utf-8"])
