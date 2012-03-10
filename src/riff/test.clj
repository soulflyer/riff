(ns riff.test
  (:use [overtone.live]
        [overtone.util.log]
        [riff.core]))

(definst beep [note 60]
  (let [src (sin-osc (midicps note))
        env (env-gen (perc 0.1 0.2) :action FREE)]
    (* src env)))

(def metro (metronome 130))

(def riffp (riff :c5 :major [:i :iii :v :i+] [0 1.5 2 3]))

(play riffp beep metro)
