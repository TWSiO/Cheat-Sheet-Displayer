(ns cheatsheetviewer.util
  ;(:require
  ;)
  )

(defn id-to-url [id]
  (if (nil? id)
    ""
    (str "#" id)
    ))

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

(defn split-id [id]
  (as-> id X
    (clojure.string/split X #"-")
    (rest X)
    (map js/parseInt X)
    (vec X)))

(defn dissoc-in [assoc-val path]
  (update-in assoc-val (butlast path) dissoc (last path))
  )

(defn get-url-sheet [url-obj]
  (as-> url-obj X
    (.-searchParams X)
    (.get X "sheet")))

(defn set-url-sheet [sheet]
  (.pushState
    js/history
    {}
    ""
    (str "/?sheet=" (:title sheet))
    ))
