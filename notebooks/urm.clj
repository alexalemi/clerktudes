;; # Universal Register Machines
;; I'm trying to follow along the Tom Hall [talk](https://youtu.be/92W3AZRs0GM)
;; and implement the universal register machine as I go along.
(ns urm
  (:require
   [clojure.pprint :refer [pprint]]
   [clojure.math.numeric-tower :as math]
   [nextjournal.clerk :as clerk]))

; ## Primitive operations
; The machine is designed to work on only three primitive instructions.

(defn end "Halt condition" [] [:end])
(defn inc "Increment a register and jump" [register jump-to] [:inc register jump-to])
(defn deb "Decrement or branch" [register jump-to branch-on-zero] [:deb register jump-to branch-on-zero])

;; and three helper instructions
(defn copy "Copy contents of one register into another." [from to goto] [:copy from to goto])
(defn push "Push the value at from into a pair at to" [from to goto] [:push from to goto])
(defn pop "Pop a value from from putting the value in to, and branch if empty." [from to goto branch] [:pop from to goto branch])

;; Apply

(declare encode-pair decode-pair)

(defn apply-statement [[instruction & args :as statement] state]
  (case instruction
    :inc (let [[register jump-to] args
               current (get-in state [:registers register] 0)]
           (-> state
               (assoc-in [:register register] (+ 1 current))
               (assoc :position jump-to)))
    :deb (let [[register jump-to branch-on-zero] args
               current (get-in state [:registers register] 0)
               branch? (zero? current)]
           (-> state
               (assoc-in [:registers register] (if branch? current (dec current)))
               (assoc :position (if branch? branch-on-zero jump-to))))
    :copy (let [[from to exit] args
                from-value (get-in state [:registers from] 0)]
            (-> state
                (assoc-in [:registers to] from-value)
                (assoc :position exit)))
    :push (let [[from to exit] args
                from-value (get-in state [:registers from] 0)
                to-value (get-in state [:registers to] 0)]
            (-> state
                (assoc-in [:registers to] (encode-pair [from-value to-value]))
                (assoc-in [:registers from] 0)
                (assoc :position exit)))
    :pop  (let [[from to halt exit] args
                from-value (get-in state [:registers from] 0)
                exit? (zero? from-value)]
            (if exit?
              (assoc state :position exit)
              (let [[h t] (decode-pair from-value)]
                (-> state
                    (assoc-in [:registers from] t)
                    (assoc-in [:registers to] h)
                    (assoc :position halt)))))
    :end state))

(defn next-state [{:keys [position program] :as state}]
  (let [next-statement (nth program position)]
    (apply-statement next-statement state)))

(defn run [state]
  (let [next (next-state state)]
    (println next)
    (if (= next state)
      state
      (recur next))))

(defn urm->fn [statements]
  (fn [& args]
    (let [final-state (run {:program statements
                            :position 0
                            :registers (zipmap (drop 1 (range)) args)})]
      (get-in final-state [:registers 0] 0))))

(defn eval-urm [statements args]
  (apply (urm->fn statements) args))

(defn zero [register]
  [(deb register 0 1)
   (end)])

(defn divides? [n div]
  (== 0 (rem n div)))

(defn factors-of-2
  ([n] (factors-of-2 n 0))
  ([n so-far]
   (if (divides? n 2)
     (recur (/ n 2)
            (+ 1 so-far))
     so-far)))

(defn encode-pair [[x y]]
  (* (math/expt 2 x)
     (+ (* 2 y) 1)))

(defn decode-pair [n]
  (let [x (factors-of-2 n)
        y (/ (dec (/ n (math/expt 2 x)))
             2)]
    [x y]))

(defn encode-pair* [pair] (dec (encode-pair pair)))
(defn uncode-pair* [n] (decode-pair (+ n 1)))

(defn encode-list [[h & t :as number-list]]
  (if (empty? number-list)
    0
    (encode-pair [h (encode-list t)])))

(defn decode-list [code]
  (if (zero? code)
    '()
    (let [[h code'] (decode-pair code)]
      (cons h (decode-list code')))))

(defn encode-instruction [[instruction register jump-to branch-on-zero]]
  (case instruction
    :inc (encode-pair [(* 2 register) jump-to])
    :deb (encode-pair [(+ (* 2 register) 1)
                       (encode-pair* [jump-to branch-on-zero])])
    :end 0))

(defn decode-instruction [code]
  (if (zero? code)
    (end)
    (let [[y z] (decode-pair code)]
      (if (even? y)
        (inc (/ y 2) z)
        (let [[j k] (uncode-pair* z)]
          (deb (/ (dec y) 2)
               j k))))))

(defn encode-program [instructions]
  (encode-list (map encode-instruction instructions)))

(defn decode-program [code]
  (map decode-instruction (decode-list code)))

(def program 1)
(def registers 2)
(def position 3)
(def current-instruction 4)
(def current-instruction-type 5)
(def current-register 6)
(def s 7)
(def t 8)
(def z 9)

(encode-list [1 2 3])
