(ns ^:figwheel-hooks lights-out.core
  (:require [clojure.string :as str]))

(defn create-items [parent num-col]
  (let [style (str "flex-basis: " (/ 100 num-col) "%;")]
    (doseq [i (range num-col)
            j (range num-col)]
      (let [elem (doto (js/document.createElement "div")
                   (.setAttribute "id" (str/join "-" ["item" i j]))
                   (.setAttribute "style" style)
                   (.classList.add "item"))]
        (set! (.-textContent elem) "X")
        (.appendChild parent elem)))))

(defn item-vec []
  (->> (js/document.querySelectorAll ".item")
       array-seq
       vec))

(defn toggle-light! [item]
  (let [current (.-textContent item)]
    (set! (.-textContent item) (if (= current "O") "X" "O"))))

(defn initialise! [items]
  (run! #(when (zero? (rand-int 2)) (toggle-light! %))
        items))

(defn completed? [items]
  (every? #(= (.-textContent %) "X") items))

(defn calculate-target-indices [item num-col]
  (let [[row column] (->> (str/split (.-id item) #"-")
                          rest
                          (map #(js/parseInt % 10)))
        max-col-idx (dec num-col)]
    (keep (fn [[r c]]
            (when (and (<= 0 r max-col-idx)
                       (<= 0 c max-col-idx))
              (+ (* num-col r) c)))
          [[(dec row) column]
           [row (dec column)]
           [row (inc column)]
           [(inc row) column]])))

(def number-of-columns 5)

(create-items (js/document.getElementById "app") number-of-columns)

(initialise! (item-vec))

(doseq [item (item-vec)]
  (.addEventListener
   item "click"
   (fn [e]
     (let [target (.-target e)
           items (item-vec)]
       (toggle-light! target)
       (run! #(toggle-light! (nth items %))
             (calculate-target-indices target number-of-columns))
       (when (completed? items)
         (js/alert "Completed 🎉"))))))
