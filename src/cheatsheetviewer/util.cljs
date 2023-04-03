(ns cheatsheetviewer.util
    ;(:require
      ;)
    )

(defn id-to-url [id]
  (str "#" id)
  )

; Not at all efficient, but not important at the moment.
(defn search-list [pred item-list]
  (first (filter pred item-list))
  )

; Only within list, not entire tree
(defn get-by-id [id item-list]
  (search-list #(= id (:id %)) item-list)
  )

(defn map-component [component items]
  (vec (map (fn [item] [component item]) items))
  )
