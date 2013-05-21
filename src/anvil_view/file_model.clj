(ns anvil-view.file-model
  (:require [anvil-clj.region-file :as rf]
            [anvil-clj.world :as world]
            [anvil-view.chunk-model :as chunk-model]))

(defn- load-file-data [save dimension file]
  (world/load-binary-file
      (world/full-path save dimension file)))

(defn- load-file-header [save dimension file]
  "Load the file header for a given file in a save / dimension"
  (rf/read-header (load-file-data save dimension file)))

(defn- make-chunk-header [[location timestamp]]
  {:location location
   :timestamp timestamp})

(defn- get-file-header- [save dimension file]
  "Load the file header and return it as a 32x32 structure
  Z/X ordered"
  (let [header (load-file-header save dimension file)]
    (vec
      (map vec
        (partition 32
          (map make-chunk-header
            (partition 2
              (interleave (:locations header) (:timestamps header)))))))))

(def get-file-header (memoize get-file-header-))

(defn unit-sectors [h]
  "Flatterns and converts sizes and offsets into 4096 units"
  (map chunk-model/chunk->sector-units (map :location (flatten h))))

(defn count-sectors [h]
  "Return the total number of 4096 byte sectors in this file
  Takes a header loaded by get-file-header
  Includes the 2 sectors used to store the header in the file"
  (reduce + 2 (map :size (unit-sectors h))))

(defn expand-sectors [{:keys [offset size]}]
  "Takes a chunk record and turns it into a list of sectors it occupies"
  (map (partial + offset) (range 0 size)))

(defn- used-sectors [h]
  "Returns all used sectors as a set.
  This expands records that are greater than size 1 too"
  (set (apply concat (map expand-sectors (unit-sectors h)))))

(defn ordered-sectors [h]
  "Take a header and convert it into a sequence of booleans
  marking if the sector is used or not.
  The two sectors used by the header are returned as empty for now"
  (map #(not (nil? %))
    (map (used-sectors h) (range 0 (count-sectors h)))))


;; nbt data

(defn get-nbt-data [save dimension file {:keys [location]}]
  (let [offset (:offset location)
        size   (:size   location)]
    (rf/read-chunk (load-file-data save dimension file) offset size)))