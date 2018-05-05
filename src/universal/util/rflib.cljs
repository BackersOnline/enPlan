(ns util.rflib
  "Extensions for re-frame"
  (:require-macros
   [cljs.core.async.macros
    :refer [go go-loop]])
  (:require
   [cljs.core.async :as async
    :refer [<! chan close! alts! timeout put!]]
   [re-frame.core :as rf]))

(defn dispatch-message
  "Dispatch re-frame message from channel"
  ([ch]
   (go
    (when-let [msg (<! ch)]
      (rf/dispatch msg))))
  ([k ch]
   (go
    (when-let [msg (<! ch)]
      (rf/dispatch [k msg])))))

(defn reg-property-sub [name]
  (rf/reg-sub name
            (fn [db [_ & path]]
              (get-in db (vec (cons name path))))))

(defn property-put-fn [name]
  (fn [db [_ & arg]]
    (let [path (cons name (butlast arg))
          value (last arg)]
      (assoc-in db path value))))

(defn reg-property-put [name]
  (rf/reg-event-db name (property-put-fn name)))

(defn reg-event-effects [name {:as effects}]
  (rf/reg-event-fx name
                   (fn [{:keys [db] :as cofx} [_ & arg]]
                     (let []
                        (->> effects
                         (map (fn [[k f]]
                                [k (apply f db arg)]))
                         (into {}))))))

(defn reg-property
  "Register re-frame dispatch and subscribe handlers for a property.
  Note that properties support access paths."
  ([name effects]
   (reg-event-effects name
                      (assoc effects
                        :db (property-put-fn name)))
   (reg-property-sub name)
   name)
  ([name]
   (reg-property-put name)
   (reg-property-sub name)
   name))

#_
(reg-property :prop)
#_
(def prop (rf/subscribe [:prop]))
#_
(list @prop)
#_
(rf/dispatch [:prop :a :b 3])

#_
(def prop1 (rf/subscribe [:prop :a]))
#_
(list @prop1)
