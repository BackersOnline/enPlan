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
   [taoensso.timbre :as timbre]
   [app.mobile.pane :as pane
    :refer [pane]]
   [app.dynamic :as dynamic]
   [util.stepper :as stepper
    :refer [step]]))

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
  (timbre/debug "DATA=" data)
  (let [changing #(rf/dispatch [:survey/response (:id data) %])
        value (get @(:survey/response session) (:id data))]
    (step {:label (:question data)
           :final (or final (not value))
           :step-index step-index}
          (case (:type data)
            "multichoice"
            [radio-field    {:name (:name data)
                             :value value
                             :options (:options data)
                             :changing changing}]
            "range"
            (let [slider-value (atom nil)]
              [ui/slider
               (assoc (:attributes data)
                      :value (or @slider-value value)

                      :on-change (fn [e val]
                                   (changing val)))])
            (timbre/warn "no matching clause for" (:type data) data))
          (if (and final value)
            [ui/raised-button
             {:label "Done"
              :on-click #(rf/dispatch [:survey/submit])}]
            [:span]))))

(defn survey [{:keys [patient] :as session}]
  (let [step-index (atom 0)]
    (fn [{:keys [patient] :as session}]
     (let [survey (:survey @patient)]
      (into [ui/stepper {:active-step @step-index
                         :orientation "vertical"}]
         (for [[ix data] (map-indexed vector survey)]
          (step-field {:ix ix
                       :data data
                       :session session
                       :step-index step-index
                       :final (= ix (dec (count survey)))}))
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
               :on-click #(rf/dispatch [:arrive/go-pickup])}]))))))

(defmethod pane "survey" [{:keys [tab patient] :as session}]
  (timbre/debug "PATIENT=" @patient)
  [ui/card {:style {:padding "1em"}}
   [:p (:feedback @patient)]
   [survey session]])
