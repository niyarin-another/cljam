(ns cljam.io.bcf.reader-test
  (:require [clojure.test :refer :all]
            [cljam.io.bcf.reader :as bcf-reader])
  (:import [java.nio ByteBuffer ByteOrder]))

(deftest parse-data-line-deep
  (are [?bytes ?var]
       (= ?var
          (@#'bcf-reader/parse-data-line-deep
           (@#'bcf-reader/read-data-line-buffer
            (doto (ByteBuffer/wrap
                   (byte-array
                    (map unchecked-byte ?bytes)))
              (.order ByteOrder/LITTLE_ENDIAN)))))
    [0x1c 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x01 0x00 0x00 0x00
     0x01 0x00 0x80 0x7f
     0x00 0x00 0x01 0x00
     0x00 0x00 0x00 0x00
     0x07
     0x17 0x4e
     0x00]
    {:chr 0, :pos 1, :id nil, :ref "N", :alt nil, :ref-length 1,
     :qual nil, :filter nil, :info [], :n-sample 0, :genotype []}

    [0x2c 0x00 0x00 0x00
     0x0a 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x01 0x00 0x00 0x00
     0x00 0x00 0x80 0x3f
     0x02 0x00 0x02 0x00
     0x02 0x00 0x00 0x02
     0x47 0x54 0x45 0x53 0x54
     0x17 0x41
     0x17 0x43
     0x11 0x00
     0x11 0x00
     0x11 0x01
     0x11 0x0a
     0x12 0x2c 0x01
     0x11 0x00
     0x11 0x00 0x01
     0x11 0x01
     0x11 0x10 0x20]
    {:chr 0, :pos 1, :id "TEST", :ref "A", :ref-length 1, :alt ["C"],
     :qual 1.0, :filter [0], :info [[0 [1]] [10 [300]]],
     :n-sample 2, :genotype [[0 [[0] [1]]] [1 [[16] [32]]]]}

    [0x28 0x00 0x00 0x00
     0x3d 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x04 0x00 0x00 0x00
     0x01 0x00 0x80 0x7f
     0x00 0x00 0x02 0x00
     0x02 0x00 0x00 0x03
     0x77
     0x46 0x4f 0x4f 0x3b 0x42 0x41 0x52
     0x47 0x41 0x41 0x41 0x43
     0x17 0x41
     0x00
     0x11 0x01
     0x67
     0x46 0x4f 0x4f 0x42 0x41 0x52
     0x42 0x41 0x5a 0x00 0x00 0x00
     0x11 0x02
     0x87
     0x46 0x4f 0x4f 0x00 0x00 0x00 0x00 0x00
     0x42 0x41 0x52 0x2c 0x42 0x41 0x5a 0x00
     0x11 0x03
     0xc7
     0x46 0x4f 0x4f 0x2c 0x42 0x41 0x52 0x2c 0x42 0x41 0x5a 0x00
     0x2e 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00]
    {:chr 0, :pos 1, :id "FOO;BAR", :ref "AAAC", :ref-length 4, :alt ["A"],
     :qual nil, :filter nil, :info [],
     :n-sample 2, :genotype [[1 [["FOOBAR"] ["BAZ"]]]
                             [2 [["FOO"] ["BAR" "BAZ"]]]
                             [3 [["FOO" "BAR" "BAZ"] [nil]]]]}

    [0x58 0x00 0x00 0x00
     0x00 0x00 0x00 0x00
     0x02 0x00 0x00 0x00
     0x22 0x00 0x00 0x00
     0x02 0x00 0x00 0x00
     0x01 0x00 0x80 0x7f
     0x03 0x00 0x03 0x00
     0x00 0x00 0x00 0x00
     0x07
     0x27 0x41 0x43
     0x47 0x41 0x43 0x43 0x43
     0x57 0x41 0x43 0x43 0x43 0x43
     0x11 0x00
     0x11 0x01
     0xf7 0x11 0x1b
     0x56 0x61 0x72 0x69 0x61 0x6e 0x74
     0x43 0x61 0x6c 0x6c
     0x46 0x6f 0x72 0x6d 0x61 0x74
     0x53 0x61 0x6d 0x70 0x6c 0x65
     0x54 0x65 0x78 0x74
     0x11 0x02
     0x77 0x46 0x4f 0x4f 0x2c 0x42 0x41 0x52
     0x11 0x04
     0x12 0x00 0x01]
    {:chr 2, :pos 35, :id nil, :ref "AC", :ref-length 2,
     :alt ["ACCC" "ACCCC"],
     :qual nil, :filter [0], :info [[1 ["VariantCallFormatSampleText"]]
                                    [2 ["FOO" "BAR"]]
                                    [4 [256]]],
     :genotype [], :n-sample 0}))
