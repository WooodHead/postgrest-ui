(ns postgrest-ui.components.item-view
  "Simple item-view component for displaying one entity based on primary key."
  (:require [postgrest-ui.impl.fetch :as fetch]
            [postgrest-ui.impl.registry :as registry]
            [postgrest-ui.display :as display]
            [postgrest-ui.elements :refer [element]])
  (:require-macros [postgrest-ui.impl.state :refer [define-stateful-component]]))


(define-stateful-component item-view [{:keys [endpoint token table select style view]} primary-key]
  {:state state}
  (if-let [defs @(registry/load-defs endpoint)]
    (if-let [{:keys [loaded-item]} @state]
      (if view
        ;; If a view component is specified, use it
        [view loaded-item]

        ;; Otherwise use generic item view
        (element style :item-view
                 (doall
                  (for [column select
                        :let [value (get loaded-item (if (map? column)
                                                       (:table column)
                                                       column))]]
                    (with-meta
                      (element style :item-view-field
                               (element style :item-view-label
                                        (display/label table column))
                               (element style :item-view-value
                                        [display/disp :item-view table column value defs]))
                      {:key column})))))

      ;; Load item
      (do
        (-> (fetch/get-by-id endpoint token defs
                             {:table table
                              :select select} primary-key)
            (.then #(swap! state merge
                           {:loaded-item %})))
        (element style :loading-indicator)))
    (element style :loading-indicator)))
