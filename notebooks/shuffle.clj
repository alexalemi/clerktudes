(ns shuffle
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]))

;; # Shuffling
;; A simple simulation of a riffle shuffle and to see how many shuffles it takes
;; for a deck to *feel* random.

;; We'll start with a deck being just a list of 52 integers.
(def deck (range 52))

;; In order to shuffle, we'll implement the [Gilbert-Shannon-Reeds
;; model](https://en.wikipedia.org/wiki/Gilbert-Shannon-Reeds_model) of riffle
;; shuffling.
;;

(rand)
