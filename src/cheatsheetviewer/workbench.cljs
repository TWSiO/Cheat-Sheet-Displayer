(ns cheatsheetviewer.workbench
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [cheatsheetviewer.util :as util]
      [cheatsheetviewer.list :as lister]
      [markdown.core :as md]
      ))

(defn workbench-display [add-to-workbench get-workbench-element remove-from-workbench workbench set-help]
  (let [
        ]
    [:div {:id "workbench-display"}
     [:h1 "Workbench"]
     (map
       (fn [item] ^{:key (:id item)} [lister/sheet-display
                                      workbench
                                      set-help
                                      add-to-workbench
                                      get-workbench-element
                                      remove-from-workbench
                                      item])
       (vals workbench))
     ]
  ))

; Would this be much easier if I just made it a generic "tree"? Would it have been worth the overhead?
(defn add-to-workbench [current-workbench sheets id-of-new]
  (let [[sheet-num section-num item-num] (util/split-id id-of-new)
        sheet (get sheets sheet-num)
        section (get (:items sheet) section-num)
        item (get (:items section) item-num)
        ]
    (swap!
      current-workbench
      #(as-> % X
         (if
           (not (contains? X sheet-num))
           (assoc X sheet-num (assoc sheet :items {}))
           X)
         (if
           (nil? (get-in X [sheet-num :items section-num]))
           (assoc-in X [sheet-num :items section-num] (assoc section :items {}))
           X)
         (assoc-in X [sheet-num :items section-num :items item-num] item)
         ))
    )
  )

(defn remove-from-workbench [current-workbench removed-id]
  (let [[sheet-num section-num item-num] (util/split-id removed-id)
        ]
  (swap!
    current-workbench
    #(as-> % X
       (util/dissoc-in X [sheet-num :items section-num :items item-num])
       (if
         (empty? (get-in X [sheet-num :items section-num :items]))
         (util/dissoc-in X [sheet-num :items section-num])
         X)
       (if
         (empty? (get-in X [sheet-num :items]))
         (dissoc X sheet-num)
         X)
      )
   )
  ))

(defn get-workbench-element [workbench id]
  (let [[sheet-num section-num item-num] (util/split-id id)
        ]
    (get-in workbench [sheet-num :items section-num :items item-num])))

(defn workbench-sidebar [go-to-item current-workbench display-workbench?]
  (let [item-on-click #(fn [sheet id event]
                         (.preventDefault event)
                         (.stopPropagation event)
                         (go-to-item sheet id))

        item-display (fn [sheet {:keys [id content]}]
                       [:div {:class "workbench-item"}
                        [:a {:href (str "?sheet=" (:title sheet) (util/id-to-url id))
                          :on-click (partial item-on-click sheet id)
                          :dangerouslySetInnerHTML {:__html (md/md->html content)}}]
                        [:button {:on-click #(remove-from-workbench current-workbench id)} "Remove"]
                        ])

        section-display (fn [sheet {:keys [id title items]}]
                          (into
                            [:div
                             [:h3
                              [:a {:href (str "?sheet=" (:title sheet) (util/id-to-url id)) :on-click (partial item-on-click sheet id)}
                               title]]]
                            (util/map-component (partial item-display sheet) (vals items))))

        sheet-display (fn [{:keys [id title items] :as sheet}]
                        (into
                          [:div {:class "workbench-sheet"} [:h2 title]]
                          (util/map-component (partial section-display sheet) (vals items))))
        ]

      [:div {:id "right-area"}
       [:section {:id "workbench"}
        [:h1 "Workbench"]
        [:input {:type "checkbox"
                 :name "workbench-toggle"
                 :checked @display-workbench?
                 :onChange #(swap! display-workbench? (fn [display?] (not display?)))}]
        [:label {:for "workbench-toggle"} "Display only workbench"]
        (into [:div {:id "workbench-sheets"}] (util/map-component sheet-display (vals @current-workbench)))
        ]
       ]
      ))

; Kind of a weird way to keep `current-workbench` atom local. It's a little janky, seems like there should be a better way, and might not be worth it, but it's currently working.
(defn workbench-component-and-setter [go-to-item display-workbench]
  (let [current-workbench (r/atom {})
        add-to-this-workbench (partial add-to-workbench current-workbench)
        ]
    [(fn [] (workbench-sidebar go-to-item current-workbench display-workbench)),
     #(workbench-display add-to-this-workbench get-workbench-element remove-from-workbench @current-workbench %)
     add-to-this-workbench
     #(get-workbench-element @current-workbench %)
     #(remove-from-workbench current-workbench %)
     ]))
