(ns app.mobile.survey
  (:require
   [reagent.core :as reagent
    :refer [atom]]
   [re-frame.core :as rf]
   [cljsjs.material-ui]
   [cljs-react-material-ui.core :as material
    :refer [get-mui-theme color]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.icons :as ic]
   [goog.string :as gstring]
   [app.mobile.pane :as pane
    :refer [pane]]
   [util.stepper :as stepper
    :refer [step]]))

(def survey-state
  (atom
   [{:question "Are you OK?"
     :options [{:label "Yes" :id "yes"}
               {:label "No" :id "no"}]}
    {:question "Do you experience a sense of burnout?"
     :options [{:label "Yes" :id "yes"}
               {:label "Maybe" :id "maybe"}
               {:label "No" :id "no"}
               {:label "Don't Know" :id "unknown"}]}]))

(defn radio-buttons [{:keys [name options changing]}]
  (into
   [ui/radio-button-group
    {:name name
     :on-change (fn [e v]
                  (changing v))}]
   (for [{:keys [label id] :as option} options]
     [ui/radio-button
      {:value id
       :label label}])))

(defn survey [{:keys [survey] :as session}]
  (let [step-index (atom 0)
        slider-value (atom 1)]
    (fn [{:keys []
          :as session}]
      [ui/stepper {:active-step @step-index
                   :orientation "vertical"}
       (step {:label (get-in @survey [0 :question])
              :final false
              :step-index step-index}
             [radio-buttons {:name "a"
                             :value (get @(:survey/response session) 0)
                             :options (get-in @survey [0 :options])
                             :changing #(rf/dispatch [:survey/response 0 %])}])
       (step {:label (get-in @survey [1 :question])
              :final false
              :step-index step-index}
             [radio-buttons {:name "b"
                             :value (get @(:survey/response session) 1)
                             :options (get-in @survey [1 :options])
                             :changing #(rf/dispatch [:survey/response 1 %])}])
       (step {:label "How exhausted are you today?"
              :final true
              :step-index step-index}
             [ui/slider
              {:value @slider-value
               :on-change (fn [e val]
                            (rf/dispatch [:survey/response 2 val]))
               :min 1
               :max 9
               :step 1}]
             [ui/raised-button
              {:label "Done"
               :on-click #(rf/dispatch [:arrive/go-pickup])}])

       #_
       (step {:label "Name" :step-index step-index}
             [ui/text-field
              {:hint-text "Type in your name"}])
       #_
       (step {:label "Group Size"  :step-index step-index}
             [ui/slider
              {:value @slider-value
               :on-change (fn [e val]
                            (rf/dispatch [:family-size val]))
               :min 1
               :max 9
               :step 1}])
       #_
       (step {:label "Get Supplies"
              :final true
              :step-index step-index}
             [ui/raised-button
              {:label "Submit Request"
               :on-click #(rf/dispatch [:arrive/go-pickup])}])])))

(defmethod pane "survey" [{:keys [tab] :as session}]
  [ui/card
   [survey (assoc session
                  :survey survey-state)]])
