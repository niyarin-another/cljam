(ns cljam.algo.convert
  "Format converter from SAM to BAM, and vice versa."
  (:require [cljam.io.sam :as sam]
            [cljam.io.bam.encoder :as encoder]
            [cljam.io.sam.util :as sam-util]
            [cljam.io.util :as io-util]
            [com.climate.claypoole :as cp])
  (:import [java.nio ByteBuffer]
           [cljam.io.bam.writer BAMWriter]))

(def ^:private default-num-block 100000)

(defn- sam-write-alignments [rdr wtr hdr num-block]
  (doseq [alns (partition-all num-block (sam/read-alignments rdr {}))]
    (sam/write-alignments wtr alns hdr)))

(defn- bam-write-alignments [rdr wtr hdr num-block]
  (let [refs (sam-util/make-refs hdr)]
    (cp/with-shutdown! [pool (cp/threadpool (cp/ncpus))]
      (doseq [blocks (cp/pmap pool
                              (fn [chunk]
                                (mapv #(let [bb (ByteBuffer/allocate (encoder/get-block-size %))]
                                         (encoder/encode-alignment bb % refs)
                                         {:data (.array bb)})
                                      chunk))
                              (partition-all num-block (sam/read-alignments rdr {})))]
        (sam/write-blocks wtr blocks)))))

(defn- _convert!
  [rdr wtr num-block write-alignments-fn]
  (let [hdr (sam/read-header rdr)]
    (sam/write-header wtr hdr)
    (sam/write-refs wtr hdr)
    (write-alignments-fn rdr wtr hdr num-block)))

(defn convert
  "Converts file format from input file to output file by the file extension."
  [in out & {:keys [num-block]
             :or {num-block default-num-block}}]
  (with-open [rdr (sam/reader in)
              wtr (sam/writer out)]
    (cond
      (io-util/sam-writer? wtr) (_convert! rdr wtr num-block sam-write-alignments)
      (io-util/bam-writer? wtr) (_convert! rdr wtr num-block bam-write-alignments)
      :else (throw (ex-info (str "Unsupported output file format " out) {}))))
  nil)