(ns cheatsheetviewer.list
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [markdown.core :as md]
      [cheatsheetviewer.util :as util]
      ))

(defn list-component [list-tag item-tag items]
  (do
    ;(println "list component" items)
    ;(println "first map" (map (fn [item] [:li item]) items))
  (into [list-tag]
   (case items
     [] nil
     (vec (map-indexed (fn [i item] ^{:key i} [:li item]) items))
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
    ;(println "examples" examples)
  [:details
   [:summary "Examples"]
   [:ul
     (map-indexed (fn [i example] ^{:key i} [:li {:class "example", :dangerouslySetInnerHTML {:__html (:content example)}}]) examples)
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

; It's all so similar, it's really tempting to make it more generic.

; Maybe add workbench in here as well, although not sure how to let users know it's in here.
(defn sheet-selection-control [set-current current-sheet sheets-info]
  (let [create-option (fn [sheet] [:option {:value (:id sheet)} (:title sheet)])
        on-change (fn [event]
                   (do
                     ;(println (str "set to " (get-by-id (.-value (.-target event)) sheets-info)))
                       (set-current (util/get-by-id (.-value (.-target event)) sheets-info))
                       ))
        ]
    (do
    (into 
      [:select {:id "sheet-selection-control" :value (:id current-sheet) :on-change on-change} ]
      (map create-option sheets-info)
      ))))

(defn left-sidebar [set-current current-sheet sheets]
  (let [toc (fn
              [{:keys [title, id]}]
              [:a {:href (util/id-to-url id)} title]
              )
        ]
    (do
      ;(println "sheet" current-sheet)
  [:div {:id "left-sidebar"}
   [sheet-selection-control set-current current-sheet sheets]
   [:nav
    (list-component :ol :li (map toc (:items current-sheet)))
    ]
   ]
  )))

(defn item-elem [sheets add-to-workbench {:keys [id, content, examples]}]
  ^{:key id} [:section {:id id, :class "element"}
              [:button {:on-click #(add-to-workbench sheets id)} "Add to workbench"]
              [:div {:dangerouslySetInnerHTML {:__html (md/md->html content)}}]
              (if (nil? examples) nil [examples-component examples])
              ]
  )

(defn sheet-section [sheets add-to-workbench {:keys [id, title, items]}]
  ^{:key id} [:section {:id id, :class "section"}
   [:h2 [:a {:href (util/id-to-url id)} title]]
   ;(map-component item-elem items)
   (map (fn [item] ^{:key (:id item)} [item-elem sheets add-to-workbench item]) items)
   ]
  )

(defn sheet-display [sheets add-to-workbench {id :id, title :title, items :items} sheet]
  (do
    ;(println "title" title)
  [:div {:id id, :class "sheet-display"}
   [:h1 title]
   ;(map-component sheet-section items)
   (map (fn [item] ^{:key (:id item)} [sheet-section sheets add-to-workbench item]) items)
   ]
  ))
