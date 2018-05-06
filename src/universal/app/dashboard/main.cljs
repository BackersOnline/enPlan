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

(defn response-table [session]
  [:table.table.table-striped.table-sm
    (into [:tbody]
     (for [[k v] @(:survey/response session)]
       [:tr [:th (str k)]
            [:td (str v)]]))])

(defn response-card [session]
  (let [reset-survey #(do (rf/dispatch [:survey/response nil])
                          (rf/dispatch [:survey/step-index 0]))]
    [:div.card
     [:div.card-body
      [:h3.card-title "Survey Response"
       [:button.button.btn.btn-danger.float-right
        {:on-click reset-survey}
        "Reset Survey"]]
      [response-table session]]]))

(defn transitions-card [session]
  (let [graph @dynamic/transition-graph
        response @(:survey/response session)
        required? #(dynamic/required? {:requires {%1 %2}} response)
        required-class #(if (required? %1 %2)
                           "badge-success"
                           "badge-primary")]
    [:div.card
     [:div.card-body
      [:h3.card-title "Transition Graph"]
      [:table.table.table-sm
       (into
        [:tbody
         [:tr [:th "Requires"][:th "Question"][:th "Provides"]]]
        (for [{:keys [requires question options id] :as item}
              (:states graph)]
          [:tr [:td (for [[k v] requires]
                      #^{:key k}
                      [:span.badge
                       {:class (required-class k v)
                        :style {:margin-right "0.3em"}}
                       (str k "=" v)])]
           [:td question]
           [:td (for [opt options]
                  #^{:key (:id opt)}
                  [:span.badge
                   {:class (required-class id (:id opt))
                    :style {:margin-right "0.3em"}}
                   (str id "=" (:id opt))])]]))]]]))

(defn view [session]
  [:div
   [transitions-card session]
   [response-card session]])
