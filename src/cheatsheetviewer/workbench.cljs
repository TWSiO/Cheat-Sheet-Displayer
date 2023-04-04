(ns cheatsheetviewer.workbench
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [cheatsheetviewer.util :as util]
      [cheatsheetviewer.list :as lister]
      ))

(defn workbench-display [add-to-workbench workbench]
  (let [
        ]
    [:div {:id "workbench-display"}
     [:h1 "Workbench"]
     (map (fn[item] ^{:key (:id item)} [lister/sheet-display workbench add-to-workbench item]) (vals workbench))
     ]
  ))

; Would this be much easier if I just made it a generic "tree"? Would it have been worth the overhead?
(defn add-to-workbench [current-workbench sheets id-of-new]
  (let [[sheet-num section-num item-num] (util/split-id id-of-new)
        sheet (get sheets sheet-num)
        section (get (:items sheet) section-num)
        item (get (:items section) item-num)
        ]
    (do
      ;(println id-of-new)
      ;(println "indexes" [sheet-num section-num item-num])
      (swap!
        current-workbench
        #(as-> % X
          (if
            (not (contains? X sheet-num))
            (assoc X sheet-num (assoc sheet :items {}))
            X)
          (do (println "Current workbench" X) X)
          (do (println "get" (get-in X [sheet-num :items section-num])) X)
          (if
            (nil? (get-in X [sheet-num :items section-num]))
            (assoc-in X [sheet-num :items section-num] (assoc section :items {}))
            X)
          (do (println "Current workbench" X) X)
          (assoc-in X [sheet-num :items section-num :items item-num] item)
          ))
    )
  ))

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

(defn workbench-sidebar [current-workbench display-workbench?]
  (let [item-display (fn [{:keys [id content]}] [:span [:button {:on-click #(remove-from-workbench current-workbench id)} "-"] [:a {:href (util/id-to-url id)} (subs content 0 15)]])
        section-display (fn [{:keys [id title items]}] (into [:div [:h3 [:a {:href (util/id-to-url id)} title]]] (util/map-component item-display (vals items))))
        sheet-display (fn [{:keys [id title items]}] (into [:div [:h2 title]] (util/map-component section-display (vals items))))
        ]
    (do
      (println "sidebar workbench" (map identity (get @current-workbench 0)))
  [:section {:id "workbench"}
   [:h1 "Workbench"]
   [:input {:type "checkbox" :name "workbench-toggle" :checked @display-workbench? :onChange #(swap! display-workbench? (fn [display?] (not display?)))}]
   [:label {:for "workbench-toggle"} "Display only workbench"]
   (into [:ul] (util/map-component sheet-display (vals @current-workbench)))
   ]
  )))

; Could I have made `current-workbench` global if I had returned a function here? Tested it out and the answer is yes.
(defn workbench-component-and-setter [display-workbench]
  (let [current-workbench (r/atom {})
        add-to-this-workbench (partial add-to-workbench current-workbench)
        ]
    [(fn [] (workbench-sidebar current-workbench display-workbench)),
     [#(workbench-display add-to-this-workbench @current-workbench)]
     add-to-this-workbench
     ]))
