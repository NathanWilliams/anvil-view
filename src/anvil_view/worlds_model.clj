(ns anvil-view.worlds-model
  (:require [anvil-clj.world :as world]))


(def dimensions [:overworld :nether :end])
(def dimension-names ["Overworld" "Nether" "End"])

(def dimension->name (zipmap dimensions dimension-names))
(def name->dimension (zipmap dimension-names dimensions))

(defn get-worlds []
  "Returns the names of Minecraft saves on this computer"
  (keys (world/list-worlds)))

(defn get-files [save dimension] ;save=world, but the name conflicts...
  "List the anvil files for a given Minecraft save and dimension
  Available dimensions are:
    :overworld
    :nether
    :end"
  (world/list-anvil-files save dimension))

;return map of dimension to availability
;we might be able to grey out an option in the combo with that
(defn get-available-dimensions [save]
  "Returns a map between dimensions and if they are populated for a given Minecraft save"
  (into {}
        (map (fn [dim] {dim (not (world/dimension-empty? save dim))})
             dimensions)))

