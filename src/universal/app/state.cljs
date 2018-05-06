(ns app.state
  (:require
   [camel-snake-kebab.core
    :refer [->kebab-case-keyword]]
   [camel-snake-kebab.extras
    :refer [transform-keys]]))

(defn transform-kebab-keys [m]
  (transform-keys ->kebab-case-keyword m))

(def state
  {:brand "enPlan"
   :mode {:current nil
          :options [{:id "split" :title "Split"}
                    {:id "mobile" :title "Mobile"}
                    {:id "dashboard" :title "Dashboard"}]}
   :mobile {:pane "survey"}
   :patient nil
   :survey/response nil
   :survey-step-index 0
   :tab {:current "main"
         :options [{:id "about" :title "About"}
                   {:id "main" :title "Main"}
                   {:id "info" :title "Info"}]}})
