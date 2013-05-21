(ns anvil-view.devel
  (:use [clojure.tools.namespace.repl :only (refresh)])
  (:require [anvil-view.view :as view]
            [seesaw.core :as sc]
            [anvil-view.controller :as controller]))

(defn show-gui []
  (sc/show! view/main-window)
  (view/init-view)
  (view/update-files)
  (view/repaint)
  (controller/setup-listeners))

