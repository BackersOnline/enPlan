(ns app.dashboard.toolbar
  (:require
   [goog.string :as gstring]
   [reagent.core :as reagent
     :refer [atom]]
   [re-frame.core :as rf]
   [taoensso.timbre :as timbre]
   [cljsjs.material-ui]
   [cljs-react-material-ui.core :as material
     :refer [get-mui-theme color]]
   [cljs-react-material-ui.reagent :as ui]
   [cljs-react-material-ui.icons :as ic]))

(defn toolbar [{:keys [tab brand]
                :as session}]
 (let [active-class #(if (and tab (= (:current @tab) %))
                       "active")]
   [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
    [:a.navbar-brand {:href "#"}
      (if brand @brand)]
    [:div.collapse.navbar-collapse
     (into
      [:ul.navbar-nav]
      (for [{:keys [id title] :as item}
            (if tab (:options @tab))]
        ^{:key id}
        [:li.nav-item {:role "presentation"
                       :class (active-class id)}
         [:a.nav-link {:on-click #(rf/dispatch [:change-tab id])}
          title]]))]]))
