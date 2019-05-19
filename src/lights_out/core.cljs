(ns ^:figwheel-hooks lights-out.core
  (:require [clojure.string :as str]))

(defn item-vec []
  (->> (js/document.querySelectorAll ".item")
       array-seq
       vec))

(defn toggle-light! [item]
  (let [current (.-innerHTML item)]
    (set! (.-innerHTML item) (if (= current "O") "X" "O"))))

(defn initialise! [items]
  (run! #(when (zero? (rand-int 2)) (toggle-light! %))
        items))

(defn completed? [items]
  (every? #(= (.-innerHTML %) "X") items))

(defn calculate-target-indices [item]
  (let [[row column] (->> (str/split (.-id item) #"-")
                          rest
                          (map #(js/parseInt % 10)))]
    (keep (fn [[r c]]
            (when (and (<= 0 r 4) (<= 0 c 4))
              (+ (* 5 r) c)))
          [[(dec row) column]
           [row (dec column)]
           [row (inc column)]
           [(inc row) column]])))

(initialise! (item-vec))

(doseq [item (item-vec)]
  (.addEventListener
   item "click"
   (fn [e]
     (let [target (.-target e)
           items (item-vec)]
       (toggle-light! target)
       (run! #(toggle-light! (nth items %))
             (calculate-target-indices target))
       (when (completed? items)
         (js/alert "Completed 🎉"))))))
