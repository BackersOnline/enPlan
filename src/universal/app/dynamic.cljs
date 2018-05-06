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
   {:states
    [{:requires nil
      :type "multichoice"
      :id "status"
      :question "Are you OK?"
      :options [{:label "Yes" :id "yes"}
                {:label "No" :id "no"}]}
     {:requires {"status" "no"}
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
     {:requires {"burnout" "maybe"}
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
     {:requires {"food" "yes"}
      :type "multichoice"
      :id "gluten"
      :question "Have you eaten anything containing cluten?"
      :options [{:label "Yes" :id "yes"}
                {:label "No" :id "no"}
                {:label "Don't know" :id "unknown"}]}
     {:requires {"status" "yes"
                 "food" "yes"}
      :type "range"
      :id "exhausted"
      :question "How exhausted are you today?"
      :attributes {:min 1
                   :max 9
                   :step 1}}]}))

(defn required? [{:keys [requires] :as node} responses]
   (or (not requires)
       (every (fn [[k v]]
               (= v (get responses k ::undefined)))
              requires)))

#_
(required? {:requires {"status" "yes"}}
           {"status" "yes"})

#_
(required? {:requires {"status" "yes"}}
           {"status" "no"})


(defn patient-state [{:as db}]
  (let [responses (:survey/response db)]
   {:feedback "Hello! It would help your care team if you answer these questions:"
    :survey
    (->> (:states @transition-graph)
         (filter #(required? % responses))
         (vec)
         (take 6))}))

#_
(patient-state {})
