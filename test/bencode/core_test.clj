(ns bencode.core-test
  (:use clojure.test
        bencode.core))

(deftest encoding-options
  (testing "Encoding output as string"
    (is (= "4:spam" (bencode "spam"))))

  (testing "Encoding output as a byte array"
    (is (= (vec (.getBytes "4:spam"))
           (vec (bencode "spam" {:raw-str? true})))))

  (testing "Encoding output to a custom stream"
    (let [stream (java.io.ByteArrayOutputStream.)]
      (bencode "spam" {:to stream})
      (is (= "4:spam" (String. (.toByteArray stream)))))))
