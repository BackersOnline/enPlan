(ns app.dashboard.main
  (:require
   [goog.string :as gstring]
   [reagent.core :as reagent
     :refer [atom]]
   [re-frame.core :as rf]
   [cljsjs.material-ui]
   [cljs-react-material-ui.core :as material
     :refer [get-mui-theme color]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.icons :as ic]
   [taoensso.timbre :as timbre]
   [app.dynamic :as dynamic]
   [app.dashboard.pane
    :refer [pane]]))

(defn reset-survey []
   (rf/dispatch [:survey/response nil])
   (rf/dispatch [:broadcast [:survey-step-index 0]]))

(defn tag-badge [session k v]
  (let [response @(:survey/response session)
        required? #(dynamic/required? {:requires {%1 %2}} response)
        required-class #(if (required? %1 %2)
                          "badge-primary"
                          "badge-secondary")]
    [:span.badge
     {:class (required-class k v)
      :style {:margin-right "0.3em"}}
     (str k "=" v)]))

(defn plan-choices-table [session]
  (let [graph @dynamic/transition-graph]
    [:table.table.table-sm
     (into
      [:tbody
       [:tr [:th "Requires"][:th "Description"]]]
      (for [{:keys [requires treatment]}
            (:plan-choices graph)]
        [:tr
         [:td (for [[k v] requires]
                #^{:key k}
                [tag-badge session k v])]
         [:td treatment]]))]))

(defn plan-choices-card [session]
  [:div.card
   [::div.card-body
    [:h3.card-title "Plan Choices"]
    [plan-choices-table session]]])

(defn transitions-table [session]
  (let [graph @dynamic/transition-graph]
    [:table.table.table-sm
       (into
        [:tbody
         [:tr [:th "Requires"][:th "question"][:th "Provides"]]]
        (for [{:keys [requires question options id] :as item}
              (:states graph)]
          [:tr [:td (for [[k v] requires]
                      #^{:key k}
                      [tag-badge session k v])]
           [:td question]
           [:td (for [opt options]
                  #^{:key (:id opt)}
                  [tag-badge session id (:id opt)])]]))]))

(defn reset-button [session]
  [:button.button.btn.btn-danger.float-right
   {:on-click reset-survey
    :disabled (if (empty? @(:survey/response session)) true)}
   "Reset Survey"])

(defn transitions-card [session]
  [:div.card
   [::div.card-body
    [:h3.card-title
     "Transition Graph"
     [reset-button session]]
    [transitions-table session]]])

(defn view [session]
  [:div
   [transitions-card session]
   [plan-choices-card session]])
