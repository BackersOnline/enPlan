(ns util.env)

(defn env [k]
  (aget js/process "env" (str k)))

(defn node? []
  (exists? goog/nodeGlobalRequire))
