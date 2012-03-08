(ns riff.core
  (:use [overtone.live]
        [overtone.util.log]))

(definst beep [note 60]
  (let [src (sin-osc (midicps note))
        env (env-gen (perc 0.1 0.2) :action FREE)]
    (* src env)))

(def metro (metronome 130))

(defprotocol IRiff
  (riff-root [this] [this new-riff-root]
    "Returns the root or sets it to new-riff-root")
  (riff-scale [this] [this new-riff-scale]
    "returns the current scale or sets it to new-scale")
  (riff-notes [this] [this new-riff-notes]
    "Returns notes or sets it to new-riff-notes")
  (riff-offsets [this] [this new-riff-offsets]
    "Returns offsets or sets it to new-riff-offsets")
  (riff-offset [this] [this new-riff-offset]
    "Returns offset or sets it to new-riff-offset"))

(deftype Riff [riff-root riff-scale riff-notes riff-offsets riff-offset]
  IRiff
  (riff-root [this] @riff-root)
  (riff-root [this new-riff-root]
    (reset! riff-root new-riff-root))
  (riff-scale [this] @riff-scale)
  (riff-scale [this new-riff-scale]
    (reset! riff-scale new-riff-scale))
  (riff-notes [this] @riff-notes)
  (riff-notes [this new-riff-notes]
    (reset! riff-notes new-riff-notes))
  (riff-offsets [this] @riff-offsets)
  (riff-offsets [this new-riff-offsets]
    (reset! riff-offsets new-riff-offsets))
  (riff-offset [this] @riff-offset)
  (riff-offset [this new-riff-offset]
    (reset! riff-offset new-riff-offset)))

(defn riff [riff-root riff-scale riff-notes riff-offsets]
  (let [riff-root (atom riff-root)
        riff-scale (atom riff-scale)
        riff-notes (atom riff-notes)
        riff-offsets (atom riff-offsets)
        riff-offset (atom 0)]
    (Riff. riff-root riff-scale riff-notes riff-offsets riff-offset)))

(def riffp (riff :c5 :major [:i :iii :v :i+] [0 1.5 2 3]))

(defn play
  ([riff metro] (play riff metro (bar metro)))
  ([riff metro br]
     (let [notes-to-play (degrees->pitches (riff-notes riff) (riff-scale riff) (riff-root riff))]
       (info "play " riff " metro " br)
       (dorun
        (map (fn [note offset]
               (at
                (+ (bar metro br) (* (tick metro) offset))
                ;; (bar metro (+ br offset))
                   (beep note)
                   (info "RiffPlayer note: " note
                         "  time: " (bar metro (+ br offset)))))
             notes-to-play
             (riff-offsets riff)))
       (apply-at (bar metro (inc br))  #'play riff [metro (inc br)]))))

(play riffp metro)
