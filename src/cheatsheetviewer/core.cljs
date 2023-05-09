(ns cheatsheetviewer.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [clojure.edn :as edn]
      [cheatsheetviewer.list :as lister]
      [cheatsheetviewer.workbench :as wb]
      [cheatsheetviewer.util :as util]
      ))

; It's based on initial load of data, so won't be good across loads.
(defn add-ids [items parent-partial-id]
  (let [create-partial-id #(str (if (nil? parent-partial-id) "" (str parent-partial-id "-")) %)
        create-new-id #(str "item-" (create-partial-id %))
        assign-id (fn [idx item] (as-> item X
                                  (assoc X :id (create-new-id idx))
                                  (assoc X :items (add-ids (:items X) (create-partial-id idx)))
                                  ))
        ]
    (vec (map-indexed assign-id items))
    ))


;(def data-id (add-ids data nil))

(defn get-sheets []
  (let [sheet-elem (.getElementById js/document "test-sheet")
        sheet-string (.-sheet (.-dataset sheet-elem))
        ]
    (edn/read-string sheet-string)
  ))

(defn set-url [sheet item-id old-url-obj]
  (let [new-search-params-obj (js/URLSearchParams. [])
        new-search-params (do
                            (.set new-search-params-obj "sheet" (:title sheet))
                            (.toString new-search-params-obj))

        new-url-obj (js/URL.
                      (str
                        (.-origin old-url-obj)
                        (.-pathname old-url-obj)
                        "?"
                        new-search-params
                        (util/id-to-url item-id)
                        ))
        new-url-part (str "?" new-search-params (util/id-to-url item-id))
        ]

      (.pushState js/history {} "" new-url-part)
      new-url-obj
    ))

(def controlled-url
  (as-> js/document X
    (.-location X)
    (.-href X)
    (js/URL. X)
    (r/atom X)
    ))

(defn help-display [set-help]
  [:div {:class "help"}
   [:a {:id "help-link" :on-click #(set-help false) :href "#"} "✖"]
   [:h1 "Help"]
   [:p "On this page you can see all of my current cheat sheets."]
   [:p "You can select which cheat sheet to view in the top left drop down menu."]
   [:p "In the left sidebar is a table of contents to quickly navigate the cheat sheet's sections."]
   [:p "In the right sidebar is the workbench. This allows you to collect items that are currently useful to what you are currently working on so you don't have to keep navigating back and forth between frequently used items for some task. You can add items to the workbench using the \"Add to workbench\" button on each item. Then you can either select the link in the workbench sidebar to navigate to it, or you can select the \"Display only workbench\" checkbox to display all of the items in your workbench in the main view area."]
   [:p "It is not currently suited for use on mobile device screen sizes."]
   ])

;; -------------------------
;; Views

; I really want to clean this up, but also want to keep the atoms local (although I guess it doesn't really matter too much).
(defn cheat-sheet-page [sheets]
  (let [url-sheet (util/get-url-sheet @controlled-url)
        current-sheet (r/atom
                        (if (nil? url-sheet)
                          (first sheets)
                          (util/search-list
                            #(= url-sheet (:title %))
                            sheets)))

        go-to-item (fn [sheet item-id]
                     (swap! controlled-url (partial set-url sheet item-id))

                     (cond
                       (nil? item-id) (.scrollTo js/window 0 0)
                       (= (:id @current-sheet) (:id sheet))
                       (.scrollIntoView
                         (.getElementById js/document item-id))
                       )

                     ; TODO Doesn't align with how I'm doing stuff elsewhere.
                     (reset!
                       current-sheet
                       (util/search-list
                         #(= (:id sheet) (:id %))
                         sheets))
                     )

        set-current (fn [sheet] (go-to-item sheet nil))

        ; TODO Probably should have this in the URL as well, but not for v1.
        display-workbench (r/atom false)

        [workbench-sidebar, workbench-display, add-to-workbench, get-workbench-element, remove-from-workbench] (wb/workbench-component-and-setter go-to-item display-workbench)

        ; TODO Probably should have this in the URL as well, but not for v1.
        help-state (r/atom false)
        set-help (fn [new-help-state]
                   (reset! help-state new-help-state)
                   )
        ]
    (fn []
      (r/after-render
        #(if (not (= (.-hash @controlled-url) ""))
           (as-> @controlled-url X
             (.-hash X)
             (subs X 1)
             (.getElementById js/document X)
             (.scrollIntoView X)
             )))
      [:div {:id "everything"}
       [lister/left-sidebar set-current @current-sheet sheets]
       [:main
        (cond
          @help-state [help-display set-help]
          @display-workbench [workbench-display set-help]
          :else [lister/sheet-display
                 sheets
                 set-help
                 add-to-workbench
                 get-workbench-element
                 remove-from-workbench
                 @current-sheet]
          )]

       [workbench-sidebar]
       ]
      )))

;; -------------------------
;; Initialize app

(def data-url
  (as-> "cheat-sheet-info" X
    (.getElementById js/document X)
    (.-dataset X)
    (.-url X)
  ))

(def cheat-sheet-data
  (as-> "cheat-sheet-json" X
      (.getElementById js/document X)
      (.-text X)
      (.parse js/JSON X)
      (js->clj X :keywordize-keys true)
      (add-ids X)
      ))

(defn mount-root []
  (d/render
    [cheat-sheet-page cheat-sheet-data]
    (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
