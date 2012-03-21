(ns riff.test
  (:use [overtone.live]
        [overtone.util.log]
        [riff.core]
        [overtone.inst.drum]))

(definst beep [note 60
               vel  0.7]
  (let [src (sin-osc (midicps note))
        env (env-gen (perc 0.1 0.2) :action FREE)]
    (* vel src env)))

(def metro (metronome 130))

(def riffp (riff :c5 :major [:i :iii :v :i+] [1 2.5 3 5]))

(defn kck [] (kick2 30 0.8 5 5 0.2 0.02))
;;(play riffp beep metro)

;;(def riffa (riff-play :c5 :minor [:ii :i :iv :i+] [1 2 3 5] beep metro))




;;(async-play riffp beep metro)

