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
  (let [split-id (vec (map js/parseInt (rest (clojure.string/split id-of-new #"-"))))
        [sheet-num section-num item-num] split-id
        sheet (get sheets sheet-num)
        section (get (:items sheet) section-num)
        item (get (:items section) item-num)
        ]
    (do
      ;(println id-of-new)
      ;(println "indexes" [sheet-num section-num item-num])
      ;(println "sheets" sheets)
      ;(println "sheet" sheet)
      ;(println "sheet-num" sheet-num)
      ;(println "get sheet" (get sheets sheet-num))
      ;(println "section" section)
      ;(println "item" item)
      ;(println "current workbench" @current-workbench)
    (cond
      ; Doesn't contain sheet
      (not (contains? @current-workbench sheet-num)) (as-> item X
                                                       (assoc-in
                                                         sheet
                                                         [:items section-num :items item-num]
                                                         X)
                                                       (fn [] (assoc @current-workbench sheet-num X))
                                                       (swap! current-workbench X)
                                                       )
      ; Doesn't contain section
      (nil? (get-in @current-workbench [sheet-num :items section-num])) (as-> item X
                                                       (assoc-in
                                                         section
                                                         [:items item-num]
                                                         X)
                                                       (fn [] (assoc-in @current-workbench [sheet-num :items section-num] X))
                                                       (swap! current-workbench X)
                                                       )
      ; Doesn't contain item
      :else (as-> item X
              (fn [] (assoc-in @current-workbench [sheet-num :items section-num :items item-num] X))
              (swap! current-workbench X)
              )
      )
    )
  ))

(defn workbench-sidebar [current-workbench display-workbench?]
  (let [item-display (fn [{:keys [id content]}] [:a {:href (util/id-to-url id)} (subs content 0 15)]) ;TODO cut off content
        section-display (fn [{:keys [id title items]}] (into [:div [:h3 [:a {:href (util/id-to-url id)} title]]] (util/map-component item-display items)))
        sheet-display (fn [{:keys [id title items]}] (into [:div [:h2 title]] (util/map-component section-display items)))
        ]
  [:section {:id "workbench"}
   [:h1 "Workbench"]
   [:input {:type "checkbox" :name "workbench-toggle" :checked @display-workbench? :onChange #(swap! display-workbench? (fn [display?] (not display?)))}]
   [:label {:for "workbench-toggle"} "Display only workbench"]
   (into [:ul] (util/map-component sheet-display (vals current-workbench)))
   ]
  ))

; Could I have made `current-workbench` global if I had returned a function here? Tested it out and the answer is yes.
(defn workbench-component-and-setter [display-workbench]
  (let [current-workbench (r/atom {})
        add-to-this-workbench (partial add-to-workbench current-workbench)
        ]
    [(fn [] (workbench-sidebar @current-workbench display-workbench)),
     [#(workbench-display add-to-this-workbench @current-workbench)]
     add-to-this-workbench
     ]))
