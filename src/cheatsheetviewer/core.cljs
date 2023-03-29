(ns cheatsheetviewer.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [clojure.edn :as edn]
      [cheatsheetviewer.list :as lister]
      ))

; Don't really need tags. Not sure when anything from clojure would cross over to scala for example and be relevent.
(def test-data
  [
   {:title "Markdown"
    :items [{:title "link"
             :items [{:content "[example](https://www.example.com)"
                      :examples [{:content "[title](https://www.example.com)"}]
                      }
                     ]
             }
            {:title "heading"
             :items [{:content "To create a heading use #"
                      }
                     ]
             }
            ]
    }
   {:title "command line"
    :items [{:title "`find`"
             :items [{:content "Manual: [https://www.gnu.org/software/findutils/manual/find.html](https://www.gnu.org/software/findutils/manual/find.html)"
                      }
                     {:content "```find <OPTIONS> <PATH>```"}
                     {:content "Options
                               * -type
                               * -name"}
                     ]
             }
            ]
    }
   {:title "Conjure"
    :items [{:title "Local Leader"
             :items [{:content "All the commands are prefixed by the `<localleader>` (`<ll>`) key. By default that is `-`."}]
             }
            {:title "Log Buffer"
             :items [{:content "Log buffer related stuff starts with `l`"}
                     {:content "s (<ll>ls): Open horizontal log buffer"}
                     {:content "v (<ll>lv): Open vertical log buffer"}
                     {:content "q (<ll>lq): Close log buffers*"}
                     ]
             }
            {:title "Evaluation"
             :items [{:content "Evaluation stuff starts with `e`"}
                     {:content "b (<ll>eb): Evaluates entire buffer (current \"file\"). Useful for evaluating all definitions."}
                     {:content "e (<ll>ee): Evaluates form (inside parens) that your cursor is on.*"}
                     {:content "r (<ll>er): Evaluates outer form that your cursor is on. Basically the outer most parens that isn't within a parens.*"}
                     {:content "w (<ll>ew): Shows the contents of a variable. More useful in scripting."}
                     ]
             }
            {:title "Tips"
             :items [{:content "To test out some things, you can type some code in temporarily and evaluate it with conjure."}
                     ]
             }
            ]
    }
   ]
  )

; Has to be depth first
;(defn dft [{items :items} node]
;  (let [aggregator (fn [aggr item] )
;        mapped (reduce aggregator [] items)
;        ]
;  )

; It's based on initial load of data, so won't be good across loads.
(defn add-ids [items parent-partial-id]
  (let [create-partial-id #(str (if (nil? parent-partial-id) "" (str parent-partial-id "-")) %)
        create-new-id #(str "item-" (create-partial-id %))
        assign-id (fn [idx item] (as-> item X
                                  (assoc X :id (create-new-id idx))
                                  (assoc X :items (add-ids (:items X) (create-partial-id idx)))
                                  ))
        ]
    (do
      ;(println "id items" items)
    (map-indexed assign-id items)
    )))


(def test-data-id (add-ids test-data nil))

(println "test-data-id" test-data-id)

(def test-data-2
  [{:content "command line"
    :children [{:content "`find`"
                :children [{:content "Manual: [https://www.gnu.org/software/findutils/manual/find.html](https://www.gnu.org/software/findutils/manual/find.html)"
                            }
                           {:content "```find <OPTIONS> <PATH>```"}
                           {:content "Options"
                            :children [{:content "-type"} {:content "-name"}]
                            }
                           ]
                }
               ]
    }
   {:content "Markdown"
    :children [{:content "link"
                :children [{:content "[]()"
                            :children [{:content "examples"
                                        :children [{:content "[title](https://www.example.com)"}
                                                   ]
                                        }]
                            }
                           ]
                }
               ]
    }
   ]
  )


(defn get-sheets []
  (let [sheet-elem (.getElementById js/document "test-sheet")
        sheet-string (.-sheet (.-dataset sheet-elem))
        ]
    (edn/read-string sheet-string)
  ))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [:p "Cheat sheets"]
   [lister/everything test-data-id]
   ])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
