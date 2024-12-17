(require '[nextjournal.clerk :as clerk])

(comment
  ;; with watching
  (clerk/serve! {:watch-paths ["notebooks"] :port 1313 :host "0.0.0.0"})

  ;; without watching
  (clerk/serve! {:port 1313 :host "0.0.0.0"}))
