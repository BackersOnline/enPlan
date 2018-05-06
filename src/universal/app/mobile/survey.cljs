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
   [app.dynamic :as dynamic]))

(defn radio-field [{:keys [name options changing]}]
  (into
   [ui/radio-button-group
    {:name (or name "???")
     :on-change (fn [e v]
                  (changing v))}]
   (for [{:keys [label id] :as option} options]
     [ui/radio-button
      {:value id
       :label label}])))

(defn step [{:keys [label final step-index]} & content]
  [ui/step
   [ui/step-label label]
   (into [ui/step-content {}]
         content)
   [ui/step-content {:style {:margin-top "1em"}}
    [:div {:style {:display (if final "none")}}
      [ui/flat-button {:label "Next"
                       :on-click #(rf/dispatch [:survey/step-index (inc step-index)])}]]]])


(defn step-field [{:keys [data final session ix]}]
  (timbre/debug "DATA=" data)
  (let [changing #(rf/dispatch [:survey/response (:id data) %])
        value (get @(:survey/response session) (:id data))]
    (step {:label (:question data)
           :final (or final (not value))
           :step-index @(:survey/step-index session)}
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
                      :value (or @slider-value
                                 value
                                 (get data [:attributes :min]))
                      :on-change (fn [e val]
                                   (changing val)))])
            (timbre/warn "no matching clause for" (:type data) data))
          (if (and final value)
            [ui/raised-button
             {:label "Done"
              :on-click #(do (rf/dispatch [:survey/step-index 0])
                             #_(rf/dispatch [:survey/response nil])
                             (rf/dispatch [:survey/submit]))}]
            [:span]))))

(defn survey [{:keys [patient] :as session}]
  (let []
    (fn [{:keys [patient] :as session}]
     (let [survey (:survey @patient)]
       (into [ui/stepper {:active-step @(:survey/step-index session)
                          :orientation "vertical"}]
         (for [[ix data] (map-indexed vector survey)]
          (step-field {:ix ix
                       :data data
                       :session session
                       :final (= ix (dec (count survey)))})))))))

(defn plan-view [{:keys [patient] :as session}]
  (let [response @(:survey/response session)
        plan (:plan @patient)]
    (if-not (empty? plan)
      (into
       [:div]
       (for [{:keys [id treatment] :as item} plan]
         #^{:key id}
         [:div treatment]))
      [:p "We have a plan. And it's a good one. But you don't need it today."])))

(defmethod pane "survey" [{:keys [tab patient] :as session}]
  (timbre/debug "PATIENT=" @patient)
  [ui/card {:style {:padding "1em"}}
   (if (or
        (empty? @(:survey/response session))
        (not= @(:survey/step-index session) 0))
     [:div
       [:p (:feedback @patient)]
       [survey session]]
     [plan-view session])])
