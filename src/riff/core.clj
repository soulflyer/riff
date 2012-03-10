(ns riff.core
  (:use [overtone.live]
        [overtone.util.log]))

(defprotocol IRiff
  (riff-root [this] [this new-riff-root]
    "Returns the root or sets it to new-riff-root")
  (riff-scale [this] [this new-riff-scale]
    "returns the current scale or sets it to new-scale")
  (riff-notes [this] [this new-riff-notes]
    "Returns notes or sets it to new-riff-notes")
  (riff-offsets [this] [this new-riff-offsets]
    "Returns offsets or sets it to new-riff-offsets")
  (riff-shift [this] [this new-riff-shift]
    "Returns shift or sets it to new-riff-shift")
  (riff-run   [this]
    "Checked to see if the riff should continue looping")
  (riff-stop [this]
    "sets the flag that will stop the riff at the end of it's current iteration"))

(deftype Riff [riff-root riff-scale riff-notes riff-offsets riff-shift riff-run]
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
  (riff-shift   [this] @riff-shift)
  (riff-shift   [this new-riff-shift]
    (reset! riff-shift new-riff-shift))
  (riff-run     [this] @riff-run)
  (riff-stop    [this]
    (reset! riff-run nil)))

(defn riff [riff-root riff-scale riff-notes riff-offsets]
  (let [riff-root    (atom riff-root)
        riff-scale   (atom riff-scale)
        riff-notes   (atom riff-notes)
        riff-offsets (atom riff-offsets)
        riff-shift   (atom 0)
        riff-run     (atom 1)]
    (Riff. riff-root riff-scale riff-notes riff-offsets riff-shift riff-run)))

(defn play
  "plays a riff over a whole number of bars dependant on the current bpb
  and the largest number in riff-offsets"
  ([riff inst metro] (play riff inst metro (bar metro)))
  ([riff inst metro br]
     (let [shifted-notes (move-degrees (riff-notes riff) (riff-shift riff))
           notes-to-play (degrees->pitches shifted-notes (riff-scale riff) (riff-root riff))
           riff-length   (+ 1 (quot (first (reverse (sort (riff-offsets riff)))) (bpb metro)))]
       (info "play " riff " metro " br)
       (dorun
        (map (fn [note offset]
               (at
                (+ (bar metro br) (* (tick metro) offset))
                ;; (bar metro (+ br offset))
                   (inst note)
                   (info "RiffPlayer note: " note
                         "  time: " (bar metro (+ br offset)))))
             notes-to-play
             (riff-offsets riff)))
       (if (riff-run riff) (apply-at (bar metro (+ riff-length br))  #'play riff inst [metro (+ riff-length br)])))))

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




























