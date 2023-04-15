(ns cheatsheetviewer.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [clojure.edn :as edn]
      [cheatsheetviewer.list :as lister]
      [cheatsheetviewer.workbench :as wb]
      [cheatsheetviewer.util :as util]
      ))

(def data
  (as-> "cheat-sheet-data" X
    (.getElementById js/document X)
    (.-dataset X)
    (.-sheet X)
  ))

(println "Data" data)

; Don't really need tags. Not sure when anything from clojure would cross over to scala for example and be relevent.
(def test-data
  [
   {;:id "item-0"
    :title "Markdown"
    :items [{;:id "item-0-0"
             :title "link"
             :items [{;:id "item-0-0-0"
                      :content "Put them in the form of `[CONTENT](URL)`"
                      :examples [{:content "[title](https://www.example.com)"}]
                      }
                     ]
             }
            {;:id "item-0-1"
             :title "heading"
             :items [{;:id "item-0-1-0"
                      :content "To create a heading use #"
                      }
                     ]
             }
            ]
    }
   {;:id "item-1"
    :title "command line"
    :description ""
    :items [{:title "File Compression with tar"
             :items [{:content "Typically want to use `tar -xf $FILE`"}
                     {:content "You can use `-v` with tar to print the file names as well."}
                     {:content "If you're not dealing with stdin, then it should auto-detect the compression type."}
                     ]
             },
            {;:id "item-1-0"
             :title "Finding files"
             :items [{;:id "item-1-0-0"
                      :content "To find from current directory:\n```\nlocate \"$PWD*/<FILE GLOB>\"\n```"}
                      {:content "`find`'s manual: [https://www.gnu.org/software/findutils/manual/find.html](https://www.gnu.org/software/findutils/manual/find.html)"}
                     {;:id "item-1-0-1"
                      :content "```\nfind <OPTIONS> <PATH>\n```"}
                     {:content "`find -type` specifies file type. Typically use \"`f`\"."}
                     {:content "`find -name`: Name of file to search for."}
                     ]
             },
            {:title "Shell/Bash Syntax"
             :items [{:content "When reading variables, prefix with `$`"}
                     {:content "When writing variables, don't prefix with `$`"}
                     {:content "If conditional uses `elif`"}
                     ]
             },
            {:title "diff"
             :items [{:content "`diff` finds the difference between two files/directories."}
                     {:content "`-q`/`--brief`: Report only the files that differ."}
                     {:content "`-r`/`--recursive`: Recursively compare directories found."}
                     ]
             },
            {:title "Rsync"
             :items [{:content "Ending directory name in `/` copies contents and without it copies the directory."}
                     {:content "`-v`: verbose"}
                     {:content "`-a`: archive. A combination of a bunch of other useful options such as recursive."}
                     {:content "`-P`: Combination of --partial which allows resumption of interrupted sync, and --progress which shows progress."}
                     {:content "`-n`: Dry run"}
                     {:content "`--delete`: Delete files from destination that aren't in source directory."}
                     ]
             },
            {:title "lsblk"
             :description "Lists devices"
             :items [{:content "Useful for finding USB drives, other external storage, etc."}
                     {:content "Listed devices are usually located in `/dev/`"}
                     ]
             },
            {:title "udisk"
             :description "Automatically mounts and unmounts a device"
             :items [{:content "Device names can be found with lsblk"}
                     {:content "Mounting: `udisksctl mount -b $DEVICE_NAME`"}
                     {:content "Unmounting: `udisksctl unmount -b $DEVICE_NAME`"}
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
                     {:content "s (&lt;ll&gt;ls): Open horizontal log buffer"}
                     {:content "v (&lt;ll&gt;lv): Open vertical log buffer"}
                     {:content "q (&lt;ll&gt;lq): Close log buffers*"}
                     ]
             }
            {:title "Evaluation"
             :items [{:content "Evaluation stuff starts with `e`"}
                     {:content "b (&lt;ll&gt;eb): Evaluates entire buffer (current \"file\"). Useful for evaluating all definitions."}
                     {:content "e (&lt;ll&gt;ee): Evaluates form (inside parens) that your cursor is on.*"}
                     {:content "r (&lt;ll&gt;er): Evaluates outer form that your cursor is on. Basically the outer most parens that isn't within a parens.*"}
                     {:content "w (&lt;ll&gt;ew): Shows the contents of a variable. More useful in scripting."}
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
    (vec (map-indexed assign-id items))
    )))


(def test-data-id (add-ids test-data nil))

;(println "test-data-id" test-data-id)

(defn get-sheets []
  (let [sheet-elem (.getElementById js/document "test-sheet")
        sheet-string (.-sheet (.-dataset sheet-elem))
        ]
    (edn/read-string sheet-string)
  ))

; I really want to clean this up, but I think that's more engineering than I need for this project and I'm trying to not overengineer this project.
(defn everything [sheets]
  (let [controlled-url (r/atom (js/URL. (.-href (.-location js/document))))
        url-sheet (util/get-url-sheet)
        current (r/atom (if (nil? url-sheet)
                          (first sheets)
                          (util/search-list #(= url-sheet (:title %)) sheets)))

        set-url (fn [sheet item-id old-url-obj]
                  (let [new-search-params (as-> (js/URLSearchParams. []) X
                                            (do (.set X "sheet" (:title sheet)) X)
                                            (.toString X)
                                            )
                        new-url-obj (js/URL. (str
                                               (.-origin old-url-obj)
                                               (.-pathname old-url-obj)
                                               "?"
                                               new-search-params
                                               (util/id-to-url item-id)
                                               ))
                        new-url-part (str "/?" new-search-params (util/id-to-url item-id))
                        ]
                    (do
                      (.pushState js/history {} "" new-url-part)
                      new-url-obj)
                    ))

        go-to-item (fn [sheet item-id]
                     (do
                       (swap! controlled-url (partial set-url sheet item-id))
                       (if (= (:id @current) (:id sheet)) (set! (.-location js/window) (.toString @controlled-url)))
                       ; TODO Doesn't align with how I'm doing stuff elsewhere.
                       (reset! current (util/search-list #(= (:id sheet) (:id %)) test-data-id))))

        set-current (fn [sheet] (go-to-item sheet nil))

        display-workbench (r/atom false)

        [workbench-sidebar, workbench-display, add-to-workbench, get-workbench-element, remove-from-workbench] (wb/workbench-component-and-setter go-to-item display-workbench)
        ]
    (fn []
      (do
        ;(println "Current sheet from url" url-sheet)
        [:div {:id "everything"}
         [lister/left-sidebar set-current @current sheets]
         [:main
          (if @display-workbench
            [workbench-display]
            [lister/sheet-display sheets add-to-workbench get-workbench-element remove-from-workbench @current])]

         [workbench-sidebar]
         ]
        ))))

;; -------------------------
;; Views

(defn home-page []
   [everything test-data-id]
   )

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
