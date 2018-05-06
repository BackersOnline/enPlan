(ns app.dashboard.info
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
   [app.dynamic :as dynamic]
   [app.dashboard.pane
    :refer [pane]]))

(defn response-table [session]
  [:table.table.table-striped.table-sm
    (into [:tbody]
     (for [[k v] @(:survey/response session)]
       [:tr [:th (str k)]
            [:td (str v)]]))])

(defn response-card [session]
  (let []
    [:div.card
     [:div.card-body
      [:h3.card-title "Survey Response"]
      [response-table session]]]))

(defn plan-view [session]
  (let [graph @dynamic/transition-graph]
    (into
     [:div]
     (for [{:keys [requires treatment]} (:plan-choices graph)]
       [:div.card
        [:div.card-body
         [:div.card-text treatment]]]))))

(defn view [session]
  [:div
   [:div.alert.alert-info
    "Doctor's dashboard to remote monitor a patient in a "
    "telehealth continuous care setting."]
   [response-card session]
   [:h3 {:style {:margin-left "1em"}}
    "Treatment Plan"]
   [plan-view session]])
