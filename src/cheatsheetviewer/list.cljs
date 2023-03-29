(ns cheatsheetviewer.list
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [markdown.core :as md]
      ))

(defn search [pred v]
  (first (filter pred v))
  )

(defn id-to-url [category id]
  (str "#" category "-" id)
  )

(defn list-component [list-tag item-tag items]
  (do
    ;(println "list component" items)
    ;(println "first map" (map (fn [item] [:li item]) items))
  (into [list-tag]
   (case items
     [] nil
     (vec (map (fn [item] [:li item]) items))
   )
  )))

;(list-component :ol ["foo" "bar"])

;(defn element
;  [{id :id, content :content, children :children}]
;  [:div {:dangerouslySetInnerHTML {:__html content}}
;   (if (nil? children) nil [list-component (map element children)])
;   ]
;  )

; Could obviously extract out the main part of this. Would need to put IDs on it.
(defn examples-component [examples]
  (do
    (println "examples" examples)
  [:details
   [:summary "Examples"]
   [:ul
     (map (fn [example] [:li {:class "example", :dangerouslySetInnerHTML {:__html (:content example)}}]) examples)
   ]
   ]
  ))

; May want a link and indicator back to the containing section.
;(defn element [id content section-id section-name examples]
;  [:div {:id id, :class "element", :dangerouslySetInnerHTML {:__html content}}]
;  )
;
;(defn section [id, title, heading, items]
;  [:section {:id id}
;   [heading
;    items
;    ]
;   ]
;  )

(defn map-component [component items]
  (map (fn [item] [component item]) items)
  )

; It's all so similar, it's really tempting to make it more generic.

; Not at all efficient, but not important at the moment.
(defn search-list [pred item-list]
  (first (filter pred item-list))
  )

; Only within list, not entire tree
(defn get-by-id [id item-list]
  (search-list #(= id (:id %)) item-list)
  )

; Maybe a dropdown
(defn sheet-selection-control [set-current current-sheet sheets-info]
  (let [create-option (fn [sheet] [:option {:value (:id sheet)} (:title sheet)])
        on-change (fn [event]
                   (do
                     (println (str "set to " (get-by-id (.-value (.-target event)) sheets-info)))
                       ;(set-current (get-by-id (.-value (.-target event)) sheets-info))
                       (set-current (second sheets-info))
                       ))
        ]
    (do
      (println "current sheet" current-sheet)
      ;(set-current (second sheets-info))
    (into 
      [:select {:value (:id current-sheet) :on-change on-change} ]
      (map create-option sheets-info)
      ))))

(defn left-sidebar [set-current current-sheet sheets]
  (let [toc (fn [section]
                    [:a {:href (id-to-url "section" "foo")} (:title section)]
                    )
        ]
    (do
      (println "sheet" current-sheet)
  [:div
   [sheet-selection-control set-current current-sheet sheets]
   [:nav
    (list-component :ol :li (map toc (:items current-sheet)))
    ]
   ]
  )))

(def workbench
  [:section ]
  )

(defn item-elem [{:keys [id, content, examples]}]
  [:section
   [:div {:id id :class "element", :dangerouslySetInnerHTML {:__html (md/md->html content)}}]
   (if (nil? examples) nil [examples-component examples])
   ]
  )

(defn sheet-section [{:keys [id, title, items]}]
  [:section {:id id, :class "section"}
   [:h2 title]
   (map-component item-elem items)
   ]
  )

(defn sheet-display [{id :id, title :title, items :items}]
  (do
    (println "title" title)
  [:main {:id id, :class "sheet-display"}
   [:h1 title]
   (map-component sheet-section items)
   ]
  ))

(defn everything [sheets]
  (let [
        foo (r/atom 0)
        current (r/atom (first sheets))
        set-current (fn [sheet] (do (println (str "setting " sheet))
                      (reset! current sheet)
                      ))
        ]
    (fn []
    (do
      (println "sheet" @current)
  [:div
    [left-sidebar set-current @current sheets]
    workbench
    [sheet-display @current]
    [:p @foo]
    [:input {:type "button" :value "foobar" :on-click #(swap! foo inc)}]
   ]
  ))))
