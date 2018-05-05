;; ATOM WORKFLOW
;; 1. ^, l to start proto pane (assuming cursor in editor)
;; 2. Execute one of these depending on focus for work:
;;   (dev.user/repl "app")
;;   (dev.user/repl "server")
;;  alt-command-b (or select + alt-command-s) to start figwheel repl
;; 3. shift-command-t to open terminal panel
;; 4. `heroku local web` in terminal pane to start server
;;     alternatively `lein run` (op port 1337)
;; 5. shift-command p `openLocal` -> opens in browser (alt openadmin)
;;     or openAdmin page http://127.0.0.1:5000/admin#/admin
;;
;; see https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl

{:repl
 {:main ^:replace dev.user ; `lein repl` fails if :main is the string but warns if it's a symbol
  :source-paths ["env"]
  :dependencies ;; all three are for proto-repl
  [[com.cemerick/piggieback "0.2.1"]
   [figwheel-sidecar "0.5.14"]
   [compliment "0.3.4"] ;; hack to avoid issues in repl, likely optional
   [org.clojure/tools.namespace "0.2.11"] ; optional?
   [org.clojure/tools.nrepl "0.2.13"] ; no difference so skip?
   [proto-repl "0.3.1"]
   [proto-repl-charts "0.3.2"]]
  :figwheel {:builds-to-start [:app :server]} ;; remove??
 ;; :repl-verbose true
  :repl-options {;; this is the nrepl-middleware that matters:
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                 :init-ns dev.user ;; will this start -main ?? No.
                ;; launches figwheel but misses interaction:
                                        ; :init (do (dev.user/activate))
                ;; :init (do (user/start) (user/repl))
                 :init (do
                         (dev.user/start)
                         (println "Launch REPL with:\n(dev.user/repl)"))
                                        ; :init (do (require 'dev.user)(dev.user/launch))
                ;; :skip-default-init true ;; optional - whether to load init.clj
                ;; :repl-verbose true
                 :timeout 9999999}}}
