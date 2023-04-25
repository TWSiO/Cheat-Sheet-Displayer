(ns cheatsheetviewer.list
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [markdown.core :as md]
      [cheatsheetviewer.util :as util]
      ))

; It's all so similar, it's really tempting to make it more generic.

; Could obviously extract out the main part of this. Would need to put IDs on it.
(defn examples-component [examples]
  [:details
   [:summary "Examples"]
   [:ul
    (map-indexed (fn [i example] ^{:key i} [:li {:class "example", :dangerouslySetInnerHTML {:__html (md/md->html (:content example))}}]) examples)
    ]
   ]
  )

; Maybe add workbench in here as well, although not sure how to let users know it's in here.
(defn sheet-selection-control [set-current current-sheet sheets-info]
  (let [create-option (fn [sheet] [:option {:value (:id sheet)} (:title sheet)])
        on-change (fn [event]
                    (as-> event X
                      (.-target X)
                      (.-value X)
                      (util/get-by-id X sheets-info)
                      (set-current X)))
        ]

    (into 
      [:select {:id "sheet-selection-control" :value (:id current-sheet) :on-change on-change}]
      (map create-option sheets-info)
      )))

(defn left-sidebar [set-current current-sheet sheets]
  (let [toc (fn
              [{:keys [title, id]}]
              ^{:key id} [:li [:a {:href (util/id-to-url id)} title]]
              )]

   [:div {:id "left-sidebar"}
    [sheet-selection-control set-current current-sheet sheets]
    [:nav
     (into [:ol]
     (vec (map toc (:items current-sheet)))
     )
     ]
    ]
  ))

(defn item-elem [sheets add-to-workbench get-workbench-element remove-from-workbench {:keys [id, content, examples]}]
  (let [workbench-toggle (if (nil? (get-workbench-element id))
                           [:button {:on-click #(add-to-workbench sheets id)} "Add to workbench"]
                           [:button {:on-click #(remove-from-workbench id)} "Remove from workbench"])
        ]

    ^{:key id} [:section {:id id, :class "element"}
                workbench-toggle
                [:div {:dangerouslySetInnerHTML {:__html (md/md->html content)}}]
                (if (nil? examples) nil [examples-component examples])
                ]
    ))

(defn sheet-section [sheets add-to-workbench get-workbench-element remove-from-workbench {:keys [id, title, items, description]}]
  (let [vec-items (if (map? items) (vals items) items)
        partial-component [item-elem sheets add-to-workbench get-workbench-element remove-from-workbench]
        component-fn (fn [item] ^{:key (:id item)} (conj partial-component item))
        description-section (if (nil? description) "" [:p description])
        ]

    (into ^{:key id} [:section {:id id, :class "section"}
                      [:h2 [:a {:href (util/id-to-url id)} title]]
                      description-section
                      ]
          (map component-fn vec-items))
    ))

; Maybe should have composed componenets to reduce prop drilling.
(defn sheet-display [sheets
                     set-help
                     add-to-workbench
                     get-workbench-element
                     remove-from-workbench
                     {:keys [id, title, items, description]} sheet
                     ]
  (let [vec-items (if (map? items) (vals items) items)
        partial-component [sheet-section sheets add-to-workbench get-workbench-element remove-from-workbench]
        component-fn (fn [item] ^{:key (:id item)} (conj partial-component item))
        description-section (if (nil? description) "" [:p description])
        ]

    [:div {:id id, :class "sheet-display"}
     [:a {:id "help-link" :on-click #(set-help true) :href "#"} "help"]
     [:h1 title]
     description-section
     (map component-fn vec-items)
     ]
    ))
