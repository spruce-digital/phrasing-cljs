(ns phrasing.ui.values
  (:require [garden.color :as c]))

;; Helpers --------------------------------------------------------------------

(defn print-hsla
  "Print an hsla string from CSSColor"
  [{:keys [hue saturation lightness alpha]}]
  (str "hsla(" hue "," saturation "%," lightness "%," alpha ")"))

(defn adjust
  "Adjust the value of a color"
  [color property amount]
  (case property
    :alpha (-> color (c/hex->hsl) (assoc :alpha amount) (print-hsla))))

;; Values ---------------------------------------------------------------------

(def nord {:white "#fff"
           :white- "#F8F9FB"
           :white-- "#ECEFF4"
           :white--- "#E5E9F0"
           :white---- "#D8DEE9"

           :black "#0f1115"
           :black- "#242933"
           :black-- "#2E3440"
           :black--- "#3B4252"
           :black---- "#434C5E"
           :black----- "#4C566A"

           :frost "#5E81AC"
           :frost- "#81A1C1"
           :frost-- "#88C0D0"
           :frost--- "#8FBCBB"

           :red "#BF616A"
           :orange "#D08770"
           :yellow "#EBCB8B"
           :green "#A3BE8C"
           :purple "#B48EAD"})

(def color {:background (nord :black-)
            :border     (nord :black----)
            :card       (nord :black--)
            :field      (nord :black----)
            :active     (nord :frost---)
            :icon       (nord :frost-)
            :label      (nord :frost)
            :logo       (nord :frost-)
            :text       (nord :white---)
            :bang       (nord :frost---)
            :link       (nord :frost-)
            ;; TODO
            :subtext "lighten($black-----, 20%)"})

(def default {:border-radius "6px"
              :font-size     "18px"
              :max-width     "720px"
              :box-shadow    (str "0 1px 4px " (adjust (nord :black) :alpha 0.4))})
