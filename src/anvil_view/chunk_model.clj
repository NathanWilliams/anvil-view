(ns anvil-view.chunk-model
  (import java.util.Date))

(defn empty-chunk? [c]
  "Checks the size of a chunk, zero sized chunks are considered empty"
  (= 0 (:size (:location c))))

(defn chunk->sector-units [{:keys [offset size]}]
  {:offset (when offset (/ offset 4096))
   :size   (when size   (/ size   4096))})

(defn chunk-sector-size [c]
  "Return the size of a chunk in 4096 byte 'sectors'"
  (:size (chunk->sector-units (:location c))))

(defn chunk-sector-location [c]
  "Return the chunk's location in terms of 4096 byte sectors"
  (:offset (chunk->sector-units (:location c))))

(defn chunk-timestamp [c]
  "Return the Chunk's last update time as a java.util.Date"
  (Date. (* 1000
            (:timestamp c))))

(defn get-chunk [header x z]
  (get-in header [z x]))