(ns sdk.pubnub
  (:require
   [cljsjs.pubnub]
   [taoensso.timbre :as timbre]
   [re-frame.core :as rf]
   [util.lib :as lib]
   [util.env :as env
    :refer [env]]))

;; Dependencies: [cljsjs/pubnub "4.1.1-0"]

(def PubNub
  (memoize
   (fn []
     (if (lib/node?)
       (js/require "pubnub")
       js/PubNub))))

(defn new-pubnub [config]
  (let [PN (PubNub)]
    (new PN (clj->js config))))

(def config {:publishKey   (or #_(env "PUBNUB_PUBLISH_KEY")
                               "pub-c-9fa72b4b-080d-4483-8f26-c10f390749ed")
             :subscribeKey (or #_(env "PUBNUB_SUBSCRIBE_KEY")
                               "sub-c-51fe3994-092b-11e8-be21-ca57643e6300")})

(def pubnub
  (memoize
   (fn
     ([]
      (new-pubnub config))
     ([config]
      (new-pubnub config)))))

(defn add-listener [pubnub args]
  (.addListener pubnub (clj->js args)))

(defn subscribe
  ([args]
   (subscribe (pubnub) args))
  ([pubnub {:keys [channels] :as args}]
   (.subscribe pubnub (clj->js args))))

(defn publish
  ([msg]
   (publish (pubnub) msg))
  ([pubnub msg]
   (.publish pubnub (clj->js msg))))

(defn demo [pubnub]
  (let [channel "demo"]
    (publish pubnub {:channel channel
                     :message "hello"})
    (add-listener pubnub
                  {:status (fn [status]
                             (timbre/info "Pubnub Status:" (js->clj status)))
                   :message (fn [message]
                              (timbre/info "Pubnub Message:" (js->clj message)))})
    (subscribe pubnub {:channels [channel]})))

#_
(demo (pubnub))

(defn register [pubnub {:keys [tag channel] :or {tag :pubnub/message}}]
  (add-listener pubnub
                {:status (fn [status]
                           (let [status (js->clj status :keywordize-keys true)]
                             (timbre/info "Pubnub Status:" status)))
                 :message (fn [event]
                            (let [{:keys [message] :as event}
                                  (js->clj event :keywordize-keys true)]
                              (timbre/info "Pubnub event:" tag event)
                              (when (= channel (:channel event))
                                (timbre/info "Pubnub message:" tag message)
                                (rf/dispatch [tag message]))))})
  (subscribe pubnub {:channels [channel]}))

(rf/reg-fx
 :pubnub/register
 (fn [{:as arg}]
   (timbre/debug "Pubnub register:" arg)
   (register (pubnub) arg)))

(rf/reg-event-fx
  :pubnub/register
  (fn [{:keys [db] :as cofx} [_ arg]]
    {:pubnub/register arg}))

(rf/reg-fx
   :pubnub/publish
   (fn [{:as event}]
     (timbre/debug "Pubnub publish:" event)
     (publish (pubnub) event)))

(rf/reg-event-fx
  :pubnub/publish
  (fn [{:keys [db] :as cofx} [_ event]]
    {:pubnub/publish event}))
