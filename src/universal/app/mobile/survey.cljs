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

(def patient-state
  (atom
   {:feedback "Hello! It would help your care team if you answer these questions:"
    :survey
    [{:type "multichoice"
      :id "status"
      :question "Are you OK?"
      :options [{:label "Yes" :id "yes"}
                {:label "No" :id "no"}]}
     {:type "multichoice"
      :id "burnout"
      :question "Do you experience a sense of burnout?"
      :options [{:label "Yes" :id "yes"}
                {:label "Maybe" :id "maybe"}
                {:label "No" :id "no"}
                {:label "Don't Know" :id "unknown"}]}
     {:type "range"
      :id "exhausted"
      :question "How exhausted are you today?"
      :attributes {:min 1
                   :max 9
                   :step 1}}]}))


(defn radio-field [{:keys [name options changing]}]
  (into
   [ui/radio-button-group
    {:name name
     :on-change (fn [e v]
                  (changing v))}]
   (for [{:keys [label id] :as option} options]
     [ui/radio-button
      {:value id
       :label label}])))

(defn step-field [{:keys [data final step-index session ix]}]
  (step {:label (:question data)
         :final final
         :step-index step-index}
     (case (:type data)
        "multichoice"
        [radio-field    {:name (:name data)
                         :value (get @(:survey/response session) (:id data))
                         :options (:options data)
                         :changing #(rf/dispatch [:survey/response (:id data) %])}]
        "range"
        (let [slider-value (atom nil)]
         [ui/slider
          (assoc (:attributes data)
                 :value (or @slider-value
                            (get @(:survey/response session) (:id data)))
                 :on-change (fn [e val]
                              (rf/dispatch [:survey/response (:id data) val])))]))
    (if final
       [ui/raised-button
        {:label "Done"
         :on-click #(rf/dispatch [:survey/submit])}]
       [:div])))

(defn survey [{:keys [survey] :as session}]
  (let [step-index (atom 0)]
    (fn [{:keys []
          :as session}]
      [ui/stepper {:active-step @step-index
                   :orientation "vertical"}
       (step-field {:ix 0
                    :data (get survey 0)
                    :session session
                    :step-index step-index})
       (step-field {:ix 1
                    :data (get survey 1)
                    :session session
                    :step-index step-index})
       (step-field {:ix 2
                    :data (get survey 2)
                    :session session
                    :step-index step-index
                    :final true})
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
  [ui/card {:style {:padding "1em"}}
   [:p (:feedback @patient-state)]
   [survey (assoc session
                  :survey (:survey @patient-state))]])
