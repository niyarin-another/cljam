(ns cljam.io.csi-test
  (:require [clojure.java.io :as cio]
            [clojure.test :refer [deftest is are]]
            [cljam.test-common :refer
             [deftest-remote
              with-before-after
              prepare-cache!
              prepare-cavia!
              clean-cache!
              http-server
              temp-dir
              small-bam-file
              test-csi-file
              test-large-vcf-csi-file]]
            [cljam.io.csi :as csi])
  (:import
   [cljam.io.csi CSI]))

(deftest about-read-index-with-error
  (is (thrown? java.io.IOException (csi/read-index small-bam-file))))

(deftest about-read-index-returns-csi-object
  (is (instance? CSI (csi/read-index test-csi-file))))

(deftest about-read-index-check-the-returning-object
  (let [csi-data ^CSI (csi/read-index test-csi-file)]
    (is (= 4 (.n-ref csi-data)))
    (is (= 14 (.min-shift csi-data)))
    (is (= 6 (.depth csi-data)))
    (is (= 3904 (get-in (.loffset csi-data) [0 1])))
    (is (= 3904 (:beg (get-in (.bidx csi-data) [0 37449 0]))))))

(deftest source-type-test
  (with-open [server (http-server)]
    (are [x] (instance? CSI (csi/read-index x))
      test-csi-file
      (cio/file test-csi-file)
      (cio/as-url (cio/file test-csi-file))
      (cio/as-url (str (:uri server) "/csi/test.csi")))))

(deftest-remote large-read-write-test
  (with-before-after {:before (do (prepare-cavia!)
                                  (prepare-cache!)),
                      :after (clean-cache!)}
    (let [temp-csi-file (cio/file temp-dir "test_temp.csi")
          r ^CSI (csi/read-index test-large-vcf-csi-file)
          _ (csi/write-index temp-csi-file r)
          w ^CSI (csi/read-index temp-csi-file)]
      (is (= (.n-ref r) (.n-ref w)))
      (is (= (.min-shift r) (.min-shift w)))
      (is (= (.depth r) (.depth w)))
      (is (= (.bidx r) (.bidx w)))
      (is (= (.loffset r) (.loffset w)))
      (is (= (.aux r) (.aux w))))))

(def ^:private aux-data
  (->> (str "02000000010000000200000000000000"
            "23000000000000000a00000063687231"
            "006368723300")
       (partition 2)
       (map #(unchecked-byte (Short/parseShort (apply str %) 16)))
       (byte-array)))

(def ^:private ^:const aux-map
  {:format 2, :col-seq 1, :col-beg 2, :col-end 0,
   :meta-char \#, :skip 0, :chrs ["chr1" "chr3"]})

(deftest tabix-aux-test
  (is (= aux-map (@#'csi/parse-tabix-aux aux-data)))
  (is (= (vec aux-data) (vec (@#'csi/create-tabix-aux aux-map)))))