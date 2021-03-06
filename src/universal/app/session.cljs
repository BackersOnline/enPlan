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
   [cljs-http.client :as http]
   [app.dynamic :as dynamic]))

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
                      {:channel "demo"
                       :message {:tab tab}})})
  (reg-property :stage)
  (reg-property :mobile)
  (reg-property :dashboard)

  (reg-property :survey/submit) ;; ##TODO: implement handler
  (reg-sub :patient
           (fn [db [_]]
             (dynamic/patient-state db)))

  (reg-property :survey-step-index)

  (reg-property
   :broadcast
   {;:dispatch (fn [_ id value]
    ;             [:foo value]
    :pubnub/publish (fn [_ [id value]]
                      (timbre/debug "Publish:" {(name id) value})
                      {:channel "value"
                       :message {(name id) value}})})
  (reg-property
   ::receive
   {:dispatch (fn [db response]
               (timbre/debug "Incoming:" response)
               (let [[k v] (first (vec response))]
                 [(keyword k) v]))})


  (reg-property :survey/response)
  (reg-property
   :survey/change-response
   {:dispatch (fn [_ id value]
                [:survey/response id value])
    :pubnub/publish (fn [_ id value]
                      {:channel "response"
                       :message {:response [id value]}})})
  (reg-property
   ::receive-response
   {:dispatch (fn [_ {[id value :as response] :response}]
                (timbre/debug "Incoming response:" id value)
                [:survey/response id value])})

  (rf/dispatch-sync [:initialize])

  (rf/dispatch [:pubnub/register {:channel "value" :tag ::receive}])
  (rf/dispatch [:pubnub/register {:channel "response" :tag ::receive-response}]))
