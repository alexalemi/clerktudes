;; # 🎄 Advent of Code 2022 - Day 10 - Cathode Ray Tube
(ns p10
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.test :as test]
            [nextjournal.clerk :as clerk]))

;; For today's puzzle we are given a set of instructions for how to manipulate
;; a single register.  The `noop` instruction just increments
;; the clock, while the `addx` instruction will add the corresponding
;; number to the register.

;; ## Data
;; First we read in the instructions in a parseable form

(def data-string (slurp "input/advent-2022-10.txt"))

(defn process [s]
  (->> s
       (str/split-lines)
       (map (fn [s] (str "(:" s ")")))
       (map edn/read-string)))

(def data (process data-string))

(def test-data (process "noop
addx 3
addx -5"))

(def test-data-2 (process (slurp "input/advent-2022-10-test.txt")))

;; ## Logic
;; For the core logic of the puzzle, we'll try to implement a function
;; `cycles` that will return a `lazy-seq` of the states of the system
;; at every clock cycle.

(def init {:clock 0 :X 1})

(defn noop [state] (update state :clock inc))

(defn addx [state dx] (-> state (update :clock inc) (update :X + dx)))

(defn cycles
  "Given a set of instructions, produce a lazy sequence of states."
  ([insts] (lazy-seq (cons init (cons init (cycles (noop init) insts)))))
  ([state insts]
   (if-let [[op dx] (first insts)]
     (let [next (noop state)]
       (case op
         :noop (lazy-seq (cons next (cycles next (rest insts))))
         :addx (let [future (addx next dx)]
                 (lazy-seq (cons next (cons future (cycles future (rest insts))))))))
     ;; empty)))
     nil)))

(defn signal-strength [{clock :clock x :X}] (* x clock))

;; Having done this, now we're told to pull out the "interesting signals"
;; which are the 20th clock cycle and every 40 after that.

(defn sum-of-interesting-signals [insts]
  (transduce
   (comp
    (drop 20)
    (take-nth 40)
    (map signal-strength))
   +
   (cycles insts)))

(test/deftest test-part-1
  (test/is (= 13140 (sum-of-interesting-signals test-data-2))))

(def ans1 (sum-of-interesting-signals data))

;; ## Part 2
;; For part 2, we now also have a CRT type monitor in place, and we'll draw
;; something on the screen if it so happens that our CRT cursor is within one space
;; of the horizontal register from the previous section.
;;
;; We'll use the clerk html viewer here to generate a `<pre>` element with the
;; result.

(defn render [data]
  (clerk/html
   [:pre {:style {:font-face "monospace" :line-height "1.0em"}}
    (->> (cycles data)
         (drop 1)
         (map
          (fn [pos {x :X}] (if (<= (abs (- pos x)) 1) \# \ ))
          (for [row (range 6) pos (range 40)] pos))
         (partition 40)
         (map (fn [cs] (apply str cs)))
         (str/join "\n"))]))

;; Let's look at the test

(render test-data-2)

;; Looks good, what about our puzzle input:

(render data)

(def ans2 "rglrbzau")

;; ## Main

(defn -test [& _]
  (test/run-tests 'p10))

(defn -main [& _]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
