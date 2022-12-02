(require '[nextjournal.clerk :as clerk])

(clerk/serve! {:paths ["*.clj"]})
