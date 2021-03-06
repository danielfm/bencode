(defproject bencode "0.2.6"
  :description "BitTorrent encoding implementation for Clojure."
  :url "https://github.com/danielfm/bencode"
  :license {:name "BSD License"
            :url "http://raw.github.com/danielfm/bencode/master/COPYING"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [commons-codec "1.8"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :test-paths ["src/test/clojure"]
  :jar-exclusions [#"fixtures/"])
