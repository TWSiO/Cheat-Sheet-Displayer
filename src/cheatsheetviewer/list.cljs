(ns cheatsheetviewer.list
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [markdown.core :as md]
      [cheatsheetviewer.util :as util]
      ))

(defn list-component [list-tag item-tag items]
  (do
  (into [list-tag]
   (case items
     [] nil
     (vec (map-indexed (fn [i item] ^{:key i} [:li item]) items))
   )
  )))

; Could obviously extract out the main part of this. Would need to put IDs on it.
(defn examples-component [examples]
  (do
  [:details
   [:summary "Examples"]
   [:ul
     (map-indexed (fn [i example] ^{:key i} [:li {:class "example", :dangerouslySetInnerHTML {:__html (:content example)}}]) examples)
   ]
   ]
  ))

; It's all so similar, it's really tempting to make it more generic.

; Maybe add workbench in here as well, although not sure how to let users know it's in here.
(defn sheet-selection-control [set-current current-sheet sheets-info]
  (let [create-option (fn [sheet] [:option {:value (:id sheet)} (:title sheet)])
        on-change (fn [event]
                   (do
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
   [:div {:id "left-sidebar"}
    [sheet-selection-control set-current current-sheet sheets]
    [:nav
     (list-component :ol :li (map toc (:items current-sheet)))
     ]
    ]
  )))

(defn item-elem [sheets add-to-workbench get-workbench-element remove-from-workbench {:keys [id, content, examples]}]
  (let [workbench-toggle (if (nil? (get-workbench-element id))
                           [:button {:on-click #(add-to-workbench sheets id)} "Add to workbench"]
                           [:button {:on-click #(remove-from-workbench id)} "Remove from workbench"])
        ]
    (do
    ^{:key id} [:section {:id id, :class "element"}
                workbench-toggle
                [:div {:dangerouslySetInnerHTML {:__html (md/md->html content)}}]
                (if (nil? examples) nil [examples-component examples])
                ]
    )))

(defn sheet-section [sheets add-to-workbench get-workbench-element remove-from-workbench {:keys [id, title, items]}]
  (let [vec-items (if (map? items) (vals items) items)
        ]
  ^{:key id} [:section {:id id, :class "section"}
   [:h2 [:a {:href (util/id-to-url id)} title]]
   ;(map-component item-elem items)
   (map
     (fn [item] ^{:key (:id item)} [item-elem
                                    sheets
                                    add-to-workbench
                                    get-workbench-element
                                    remove-from-workbench
                                    item])
     vec-items)
   ]
  ))

; Maybe should have composed componenets to reduce prop drilling.
(defn sheet-display [sheets add-to-workbench get-workbench-element remove-from-workbench {id :id, title :title, items :items} sheet]
  (let [vec-items (if (map? items) (vals items) items)
        ]
    (do
      [:div {:id id, :class "sheet-display"}
       [:h1 title]
       (map
         (fn [item] ^{:key (:id item)} [sheet-section
                                        sheets
                                        add-to-workbench
                                        get-workbench-element
                                        remove-from-workbench
                                        item])
         vec-items)
       ]
      )))
