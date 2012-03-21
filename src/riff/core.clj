(ns riff.core
  (:use [overtone.live]
        [overtone.util.log]))

(defprotocol IRiff
  (riff-root    [this] [this new-riff-root]
    "Returns the root or sets it to new-riff-root")
  (riff-scale   [this] [this new-riff-scale]
    "returns the current scale or sets it to new-scale")
  (riff-notes   [this] [this new-riff-notes]
    "Returns notes or sets it to new-riff-notes")
  (riff-offsets [this] [this new-riff-offsets]
    "Returns offsets or sets it to new-riff-offsets")
  (riff-vels    [this] [this new-riff-vels]
    "Returns or sets velocities")
  (riff-shift   [this] [this new-riff-shift]
    "Returns shift or sets it to new-riff-shift")
  (riff-run     [this]
    "Checked to see if the riff should continue looping")
  (riff-stop    [this]
    "sets the flag that will stop the riff at the end of it's current iteration")
  (riff-start   [this]
    "sets the flag that allows the riff to continue repeating"))

(deftype Riff [riff-root riff-scale riff-notes riff-offsets riff-vels riff-shift riff-run]
  IRiff
  (riff-root    [this] @riff-root)
  (riff-root    [this new-riff-root]
    (reset! riff-root new-riff-root))
  (riff-scale   [this] @riff-scale)
  (riff-scale   [this new-riff-scale]
    (reset! riff-scale new-riff-scale))
  (riff-notes   [this] @riff-notes)
  (riff-notes   [this new-riff-notes]
    (reset! riff-notes new-riff-notes))
  (riff-offsets [this] @riff-offsets)
  (riff-offsets [this new-riff-offsets]
    (reset! riff-offsets new-riff-offsets))
  (riff-vels    [this] @riff-vels)
  (riff-vels    [this new-riff-vels]
    (reset! riff-vels new-riff-vels))
  (riff-shift   [this] @riff-shift)
  (riff-shift   [this new-riff-shift]
    (reset! riff-shift new-riff-shift))
  (riff-run     [this] @riff-run)
  (riff-stop    [this]
    (reset! riff-run nil))
  (riff-start   [this]
    (reset! riff-run 1)))

(defn riff [riff-root riff-scale riff-notes riff-offsets]
  (let [length       (count riff-notes)
        riff-root    (atom riff-root)
        riff-scale   (atom riff-scale)
        riff-notes   (atom riff-notes)
        riff-offsets (atom riff-offsets)
        riff-vels    (atom (repeat length 0.7))
        riff-shift   (atom 0)
        riff-run     (atom 1)]
    (Riff. riff-root riff-scale riff-notes riff-offsets riff-vels riff-shift riff-run)))

(def metro (metronome 130))

(defn play
  "plays a riff over a whole number of bars dependant on the current bpb
  and the largest number in riff-offsets. I f the metro parameter is not given
  it assumes a metronome called metro has already been defined."
  ([riff inst] (play riff inst metro (bar metro)))
  ([riff inst metro] (play riff inst metro (bar metro)))
  ([riff inst metro br]
     (let [shifted-notes (move-degrees (riff-notes riff) (riff-shift riff))
           notes-to-play (degrees->pitches shifted-notes (riff-scale riff) (riff-root riff))
           riff-length   (+ 1 (quot (- (first (reverse (sort (riff-offsets riff)))) 1) (bpb metro)))]

       (dorun
        (map (fn [note offset vel]
               (let [corrected-offset (if (< 1 offset) (- offset 1) 0)]
                 (at
                 (+ (bar metro br) (* (tick metro) corrected-offset))
                 (inst note vel))))
             notes-to-play
             (riff-offsets riff)
             (riff-vels riff)))
       (if (riff-run riff)
         (apply-at (bar metro (+ riff-length br))
                   #'play riff inst [metro (+ riff-length br)])
         (riff-start riff)))))



(defn async-play
  "plays a riff that repeats on the beat but independantly of the bar structure."
  ([riff inst metro] (async-play riff inst metro (beat metro)))
  ([riff inst metro bt]
     (let [shifted-notes (move-degrees (riff-notes riff) (riff-shift riff))
           notes-to-play (degrees->pitches shifted-notes (riff-scale riff) (riff-root riff))
           riff-length   (+ 1 (first (reverse (sort (riff-offsets riff)))))]
       (info "play " riff " metro " bt)
       (dorun
        (map (fn [note offset]
               (at
                (+ (beat metro bt) (* (tick metro) offset))
                ;; (bar metro (+ br offset))
                   (inst note)
                   (info "RiffPlayer note: " note
                         "  time: " (beat metro (+ bt offset)))))
             notes-to-play
             (riff-offsets riff)))
       (if (riff-run riff) (apply-at (beat metro (+ riff-length bt))  #'async-play riff inst [metro (+ riff-length bt)])))))

;; Failed experiment to combine riff and play. Can't change anything on the fly. Might work if
;; I use recur instead of reursion, as long as apply-at plays nicely with recur. It might not.
;;
;; (defn riff-play
;;   ([riff-root riff-scale riff-notes riff-offsets inst metro]
;;      (riff-play riff-root riff-scale riff-notes riff-offsets inst metro (bar metro)))

;;   ([riff-root riff-scale riff-notes riff-offsets inst metro br]
;;      (let [length        (count riff-notes)
;;            riff-root     (atom riff-root)
;;            riff-scale    (atom riff-scale)
;;            riff-notes    (atom riff-notes)
;;            riff-offsets  (atom riff-offsets)
;;            riff-vels     (atom (repeat length 0.7))
;;            riff-shift    (atom 0)
;;            riff-run      (atom 1)
;;            shifted-notes (move-degrees @riff-notes @riff-shift)
;;            notes-to-play (degrees->pitches shifted-notes @riff-scale @riff-root)
;;            riff-length   (+ 1 (quot (- (first (reverse (sort @riff-offsets))) 1) (bpb metro)))]

;;      (info "riff starting" (now))
;;      (dorun
;;       (map (fn [note offset vel]
;;              (let [corrected-offset (if (< 1 offset) (- offset 1) 0)]
;;                (at
;;                 (+ (bar metro br) (* (tick metro) corrected-offset))
;;                 (inst note vel))))
;;            notes-to-play
;;            @riff-offsets
;;            @riff-vels))
;;      (if @riff-run
;;        (apply-at (bar metro (+ riff-length br))
;;                  #'riff-play [@riff-root @riff-scale @riff-notes @riff-offsets inst metro (+ riff-length br)]))
;;      (Riff. riff-root riff-scale riff-notes riff-offsets riff-vels riff-shift riff-run))))





































