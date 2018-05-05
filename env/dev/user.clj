(ns dev.user
  (:require
   [cemerick.piggieback]
   [figwheel-sidecar.repl-api]))

;(require 'cemerick.piggieback)
;(require 'figwheel-sidecar.repl-api)

(defn start []
  (figwheel-sidecar.repl-api/start-figwheel!))

(defn repl [& [id]]
  (figwheel-sidecar.repl-api/cljs-repl id))

(defn repl-env []
  (figwheel-sidecar.repl-api/repl-env))

(defn figwheel-running? []
  (figwheel-sidecar.repl-api/figwheel-running?))

(defn activate []
  ;; better if this accepted empty args!!
  (figwheel-sidecar.system/start-figwheel-and-cljs-repl!
   (figwheel-sidecar.system/fetch-config)))

(defn launch []
  (start)
  (assert (figwheel-running?))
  #_(println "*** STARTED FIGWHEEL ***")
  ;; should ensure completion before continouing...
  #_(Thread/sleep 10000)
  #_(println "*** STARTING REPL ***")
  (repl)
  #_(println "*** NEVER GETS HERE ***"))

; (launch)

(defn main [& args]
  (println "---------- Launching MAIN")
  (launch))

(defn -main [& args]
  (println "---------- Launching -MAIN")
  (launch))

#_
(when (figwheel-running?)
  (println "--- LAUNCH REPL ---")
  (repl))
