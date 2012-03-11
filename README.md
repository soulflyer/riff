####Riff

Riff specifies patterns to be played starting at the begining of a bar.

Note that it depends on a version of metronome that returns bar number.
This is currently in soulflyer/overtone (branch metronome) and not yet
in the main overtone repository.

Usage:
First define a riff

```clojure
(def riff-a (riff :c5 :major [:i :iii :v :i+] [1 2.5 3 4])
```
Then play it (starting at the next bar start)

```clojure
(play riff-a beep metro)
```
Change the key of the running riff:

```clojure
(riff-scale riff-a :minor)
```
Move all the notes in the riff up by 3 steps of the current scale, staying
within the scale. ie. [:i :iii :v :i+] becomes [:iv :vi :i+ :iv+]
This could be used to echo a motif, or to create a parrallel harmony.

```clojure
(riff-offset riff-a 3)
```
Change the notes of the running riff, starting at the beginning of the next bar

```clojure
(riff-notes riff-a [:i :v :iii :i+])

```


