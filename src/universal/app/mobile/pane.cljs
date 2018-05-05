(ns app.mobile.pane
  (:require
   [reagent.core :as reagent
    :refer [atom]]
   [re-frame.core :as rf]
   [cljsjs.material-ui]
   [cljs-react-material-ui.core :as material
    :refer [get-mui-theme color]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.icons :as ic]
   [goog.string :as gstring]))

(defmulti pane (fn [{:keys [stage] :as session}]
                  (if stage [@stage])))

(defmethod pane :default [{:keys [tab] :as session}]
  [ui/card
   (into [:ul.list-group {:style {:margin-top "1em"}}]
         (for [{:keys [id title] :as item}
               (if tab (:options @tab))]
           [:a.list-group-item
            {:class (if (and tab (= id (:current @tab)))
                       "active")
             :href (str "/#/tab/" id)}
            title]))])
