(ns anvil-view.view
  (:require [seesaw.core :as sc]
            [seesaw.graphics :as sg]
            [seesaw.color :as scolor]
            [seesaw.table :as st]
            [seesaw.tree :as stree]
            [anvil-view.worlds-model :as worlds]
            [anvil-view.file-model :as file-model]
            [anvil-view.chunk-model :as chunk-model]))

;; Config
(def title "Anvil Viewer")
(def window-size {:width 1000
                  :height 800})


;; Styling
(def used-style
  (sg/style :foreground (scolor/color :black)
            :background (scolor/color :green)))

(def empty-style
  (sg/style :foreground (scolor/color :black)))

(def highlight-style
  (sg/style :foreground (scolor/color :black)
            :background (scolor/color :orange)))

;; State!
(def highlight-sectors (atom []))


;forward declaration
(def main-window)

;; TODO: I need a way to refresh this
(def worldlist
  "A list of worlds on this computer"
  (worlds/get-worlds))

(def files ["r.0.0.mca"])


(defn get-dimension []
  (worlds/name->dimension
   (sc/value (sc/select main-window [:#dimension]))))

(defn get-world []
  (sc/value (sc/select main-window [:#worldlist])))

(defn get-file []
  (sc/value (sc/select main-window [:#files])))

(defn update-files []
  (sc/config! (sc/select main-window [:#files])
              :model (worlds/get-files (get-world) (get-dimension))))

(def world-selector
  "Used to select the world, dimension and anvil file to view"
  (sc/horizontal-panel
     :items [(sc/combobox :model worldlist               :id :worldlist)
             (sc/combobox :model worlds/dimension-names  :id :dimension)
             (sc/combobox :model files                   :id :files)]))



(def header-canvas
  (sc/canvas :id         :headercanvas
             :background :white))

(def sector-canvas
  (sc/canvas :id         :sectorcanvas
             :background :white))

(def header-info
  (sc/table
   :model [:columns [{:key :var   :text "Variable"}
                     {:key :value :text "Value"}]

           :rows    [{:var "Offset"    :value "0"}
                     {:var "Size"      :value "0"}
                     {:var "Timestamp" :value "0"}
                     {:var "X"         :value "0"}
                     {:var "Z"         :value "0"}]]))


(defn tree-renderer [renderer data]
  (let [v (:value data)]
    ;(println "DEBUG: " v)
    (sc/config! renderer :text (if (map? v)
                                 (format "%s(%s)" (:tag-name v) (name (:tag-type v)))
                                 (str v)))))

;(def sector-info (sc/tree :id       :nbt-tree))
(def sector-info "TODO!")

(def header-panel
  (sc/grid-panel :columns 2
                 :items [header-canvas
                         header-info]))

(def sector-panel
  (sc/grid-panel :columns 2
                 :items [sector-canvas
                         (sc/scrollable sector-info)]))

(def window-content
  (sc/border-panel
     :north  world-selector
     :center (sc/vertical-panel
                :items [header-panel sector-panel])))

(def main-window
  (sc/frame :title title
            :width (:width window-size)
            :height (:height window-size)
            :content window-content))


(defn selected-header []
  (file-model/get-file-header (get-world)
                              (get-dimension)
                              (get-file)))

;; Painting
;; Header

(defn render-record [row col rec]
  [(sg/rect (* 10 col) (* 10 row) 8 8)
   (if (chunk-model/empty-chunk? rec)
     empty-style
     used-style)])

(defn render-header []
  (apply concat
    (map-indexed
     (fn [z-index rowdata] (map-indexed (partial render-record z-index) rowdata))
         (selected-header))))

(defn paint-header [context graphics]
  (apply sg/draw graphics
    (apply concat (render-header))))



;; Sector painting

(def sectors-per-row 40)

(defn render-sector [i s]
  (let [x (rem i sectors-per-row)
        y (quot i sectors-per-row)]
    [(sg/rect (* 10 x) (* 10 y) 8 8)
     (cond (contains? @highlight-sectors i) highlight-style
           s                                used-style
           :default                         empty-style)]))



(defn render-sectors []
  (apply concat
    (map-indexed render-sector
       (file-model/ordered-sectors (selected-header)))))

(defn paint-body [context graphics]
  (apply sg/draw graphics (render-sectors)))


(defn repaint []
  (sc/repaint! header-canvas)
  (sc/repaint! sector-canvas))

;; init

(defn init-view []
  ; Config the paint functions etc
  (sc/config! header-canvas :paint paint-header)
  (sc/config! sector-canvas :paint paint-body))


;; Translations
(defn pos->header [point]
  (let [x (.x point)
        y (.y point)]
    [ (when (< x 320) (quot x 10))
      (when (< y 320) (quot y 10))]))


;; Table updates

(defn update-header-info [x z]
  (when-let [c (chunk-model/get-chunk (selected-header) x z)]
    (st/update-at! header-info 0 {:value (chunk-model/chunk-sector-location c)}
                               1 {:value (chunk-model/chunk-sector-size c)}
                               2 {:value (chunk-model/chunk-timestamp c)}
                               3 {:value x}
                               4 {:value z})))

(defn update-highlighted [x z]
  (reset! highlight-sectors
          (set
           (file-model/expand-sectors
             (chunk-model/chunk->sector-units
              (:location
                (chunk-model/get-chunk (selected-header) x z)))))))

(defn load-nbt-data [x z]
  (file-model/get-nbt-data (get-world)
                           (get-dimension)
                           (get-file)
                           (chunk-model/get-chunk (selected-header) x z)))


;; TODO: Clean this up!
(defn load-nbt-tree [x z]
  (stree/simple-tree-model
     (fn [x] (or (coll? x) (keyword? x)))
     (comp :payload)
     (load-nbt-data x z)))




;; Updates from the controller
(defn update-selected-header [x z]
  (update-header-info x z)
  (update-highlighted x z)
  ;(sc/config! sector-info :model (load-nbt-tree x z))
  ;(sc/config! sector-info :renderer tree-renderer) ;; Disabled for now as it isn't quite right
  (repaint))





