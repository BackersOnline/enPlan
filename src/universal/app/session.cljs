(ns app.session
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<!]]
   [reagent.core :as reagent]
   [re-frame.core :as rf
    :refer [reg-sub]]
   [util.rflib :as rflib
    :refer [reg-property]]
   #_[re-frame.http-fx]
   [taoensso.timbre :as timbre]
   [cljs-http.client :as http]))

(def interceptors [#_(when ^boolean js/goog.DEBUG debug)
                   rf/trim-v])

(defn state [initial]
  (->> initial
       (map #(vector (first %)(reagent/atom (second %))))
       (into {})))

(defn subscriptions [ks]
  (into {} (map #(vector % (rf/subscribe [%])) ks)))

(defn initialize [initial]

  (rf/reg-event-db
   :initialize
   (fn [db _] initial))

  (rf/reg-event-db
   :update
   (fn [db [_ path f]]
     (update-in db path f)))

  (rf/reg-event-db
   :assign
   (fn [db [_ path value]]
     (timbre/debug "Assign:" path value)
     (assoc-in db path value)))

  (reg-property :brand)
  (reg-property :mode)
  (reg-property :tab)
  (reg-property
   :change-tab
   {:dispatch (fn [_ tab] [:tab :current tab])
    :pubnub/publish (fn [_ tab]
                      {:channel "demo" :message {:tab tab}})})
  (reg-property :stage)
  (reg-property :mobile)
  (reg-property :dashboard)

  (rf/dispatch-sync [:initialize]))
