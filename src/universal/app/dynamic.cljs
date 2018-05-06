(ns app.dynamic
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [reagent.core :as reagent
    :refer [atom]]
   [reagent.ratom
    :refer [reaction]]
   [taoensso.timbre :as timbre]))

(def transition-graph
  (atom
   {:plan-choices
    [{:requires {"food" "no"}
      :id "eat"
      :treatment "Eat a healthy meal."
      :provides [{"eath" "meal"}]}
     {:requires {"status" "negative"}
      :id "walk"
      :treatment "Take a walk for at least 1/2 hour then relax and meditate."
      :provides [{"walk" "1/2 hour"}]}]
    :presets {"helix:HLA-rs2187668" "TT"
              "fitbit:steps" "<3000"}
    :states
    [{:requires nil
      :type "multichoice"
      :id "status"
      :question "Are you feeling OK?"
      :options [{:label "Yes" :id "positive"}
                {:label "No" :id "negative"}]}
     {:requires {"status" "negative"}
      :type "multichoice"
      :id "burnout"
      :question "Do you experience a sense of burnout?"
      :options [{:label "Yes" :id "yes"}
                {:label "Maybe" :id "maybe"}
                {:label "No" :id "no"}
                {:label "Don't Know" :id "unknown"}]}
     {:requires {"burnout" "yes"}
      :type "multichoice"
      :id "tired"
      :question "Are you currently feeling tired?"
      :options [{:label "Very" :id "very"}
                {:label "Somewhat" :id "somewhat"}
                {:label "Not at all" :id "no"}]}
     {:requires {"burnout" "maybe"
                 "fitbit:steps" "<3000"}
      :type "multichoice"
      :id "fatigued"
      :question "Are you currently feeling fatigued?"
      :options [{:label "Very" :id "very"}
                {:label "Somewhat" :id "somewhat"}
                {:label "Not at all" :id "no"}]}
     {:requires nil
      :type "multichoice"
      :id "food"
      :question "Have you eaten yet today?"
      :options [{:label "Yes" :id "yes"}
                {:label "No" :id "no"}]}
     {:requires {"food" "yes"
                 "helix:HLA-rs2187668" "TT"}
      :type "multichoice"
      :id "gluten"
      :question "Have you eaten anything containing cluten?"
      :options [{:label "Yes" :id "yes"}
                {:label "No" :id "no"}
                {:label "Don't know" :id "unknown"}]}
     {:requires {"status" "positive"
                 "food" "yes"}
      :type "range"
      :id "happy"
      :question "How happy are you today?"
      :attributes {:min 1
                   :max 9
                   :step 1}}]}))


(defn required? [{:keys [requires] :as node} responses]
   (or (not requires)
       (every? (fn [[k v]]
                (= v (get responses k ::undefined)))
               requires)))

#_
(required? {:requires {"status" "yes"}}
           {"status" "yes"})

#_
(required? {:requires {"status" "yes"}}
           {"status" "no"})


(defn patient-state [{:as db}]
  (let [presets (:presets @transition-graph)
        responses (:survey/response db)]
   {:feedback "Hello! It would help your care team if you answer these questions:"
    :plan
    (->> (:plan-choices @transition-graph)
         (filter #(required? % (merge responses presets)))
         (vec))
    :survey
    (->> (:states @transition-graph)
         (filter #(required? % (merge responses presets)))
         (vec)
         (take 5))}))

#_
(patient-state {})
