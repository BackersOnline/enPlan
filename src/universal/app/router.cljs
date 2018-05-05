(ns app.router
  (:require
   [cljs.core.async :as async
    :refer [<!]]
   [goog.dom :as dom]
   [goog.events :as events]
   [goog.history.EventType :as EventType]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [secretary.core :as secretary
    :refer-macros [defroute]]
   [app.routes :as routes
    :refer [routes]]
   [app.session :as session])
  (:import
   [goog History]))

(secretary/set-config! :prefix "#")

(def history
  (memoize #(History.)))

(defn navigate! [token & {:keys [stealth]}]
  (if stealth
    (secretary/dispatch! token)
    (.setToken (history) token)))

(defn hook-browser-navigation! []
  (doto (history)
    (events/listen EventType/NAVIGATE
                   (fn [event]
                     (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroute "/" []
  (rf/dispatch [:mode :current nil]))

(defroute "/split" []
  (rf/dispatch [:mode :current "split"]))

(defroute "/dashboard" []
  (rf/dispatch [:mode :current "dashboard"]))

(defroute "/mobile" []
  (rf/dispatch [:mode :current "mobile"]))

(defroute "/tab/:id" [id]
  (rf/dispatch [:change-tab id]))
