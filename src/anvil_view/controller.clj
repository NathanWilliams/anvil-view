(ns anvil-view.controller
  (:require [seesaw.core :as sc]
            [anvil-view.view :as view]))


(defn world-changed [e]
  ;In future this might also update the populated dimensions
  (view/update-files)
  (view/repaint))

(defn dimension-changed [e]
  (view/update-files)
  (view/repaint))

(defn file-changed [e]
  ;need to trigger a repaint...
  (view/repaint))


(defn header-mouse-moved [e]
  (apply view/update-selected-header (view/pos->header (.getPoint e))))


(defn setup-listeners []
  (sc/listen (sc/select view/main-window [:#worldlist])
             :selection world-changed)
  (sc/listen (sc/select view/main-window [:#dimension])
             :selection dimension-changed)
  (sc/listen (sc/select view/main-window [:#files])
             :selection file-changed)
  (sc/listen view/header-canvas
             :mouse-moved header-mouse-moved))