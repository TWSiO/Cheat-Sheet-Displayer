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
    (clojure.edn/read-string X)
  ))

(def test-data-old
  [
   {;:id "item-0"
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
   {:title "Vim"
    :items [{:title "Text Objects"
             :items [{:content "Operator + Text Object/Motion"}
                     {:content "A text object is `a` or `i` followed by a letter."}
                     {:content "`a`: \"A _\". Includes relevant surrounding items."
                      :examples [{:content "`da[` deletes everything between square brackets as well as square brackets"},
                                 {:content "`daw` deletes a word, as well as one of the spaces surrounding."}
                                 ]}
                     {:content "`i`: \"Inner _\". Just the inner content, excluding surrounding stuff."
                      :examples [{:content "`di[` deletes everything between the nearest square brackets but not the brackets themselves."}
                                 {:content "`diw` deletes the \"inner\" word which leaves the surrounding whitespace."}
                                 ]
                      }
                     {:content "`s`: Sentence"}
                     {:content "`p`: Paragraph"}
                     {:content "`[`/`]`: Text enclosed by square brackets."}
                     {:content "`(`/`)`: Text enclosed by parenthesis."}
                     {:content "`<`/`>`: Text enclosed by angle brackets."}
                     {:content "`{`/`}`: Text enclosed by curly braces."}
                     {:content "`\"`/`'`/`\\``: Text enclosed by the various quotes."}
                     {:content "`t`: Text enclosed by matching HTML/XML tags."
                      :examples [{:content "`dit` between `<p>` tags would get rid of everything between them, while leaving the tags."}]
                      }
                     ]
             },
            {:title "Multiple repeats"
             :items [{:content "`:[range]g[lobal]/{pattern}/[cmd]`"}
                     ]
             },
            {:title "Shell"
             :items [{:content "`:r![shell]` outputs shell command to the buffer (usually current file)."}
                     ]
             },
            {:title "Miscellaneous"
             :items [{:content "`<tab>`/`CTRL-I`: Opposites of `CTRL-O`"}
                     {:content "`[*`/`]*`: Go to the next start/end of a C style comment (`/*`/`*/`)."}
                     ]
             }
            ]
    }
   {:title "Clojure"
    :items [{:title "Other Resources"
             :items [{:content "Official basics page: [https://clojure.org/guides/learn/clojure](https://clojure.org/guides/learn/clojure)"}
                     {:content "Clojuredocs: [https://clojuredocs.org/](https://clojuredocs.org/)"}
                     {:content "Clojuredocs quick reference: [https://clojuredocs.org/quickref](https://clojuredocs.org/quickref)"}
                     {:content "Destructuring: [https://clojure.org/guides/destructuring](https://clojure.org/guides/destructuring)"}
                     {:content "A clojurescript resource: [https://www.learn-clojurescript.com/](https://www.learn-clojurescript.com/)"}
                     {:content "Babashka: [https://book.babashka.org/](https://book.babashka.org/)"}
                     ]
             },
            {:title "Common Higher Order Function Behavior"
             :description "E.g. `map`, `filter`, `reduce`."
             :items [{:content "The functions are often the earlier parameters."}
                     {:content "`map`, `filter`, `reduce` are curried. E.g. `(map f)` returns back a function that maps `f`"}
                     {:content "`map`, `filter`, `reduce` return lists, even if applied over vectors."}
                     ]
             },
            {:title "`get`"
             :items [{:content "Used for both vectors and maps, but not lists."}
                     {:content "Value, then key/index"
                      :examples [{:content "`(get collection idx-or-key)`"}]
                      }
                     ]
             },
            {:title "`assoc`"
             :items [{:content "`(assoc map key val)`"}
                     ]
             },
            {:title "Common Mistakes"
             :items [{:content "`#([...])` does not work and throws an error. `#(identity [...])`, `(fn [...] [...])`, `#(vec ...)`."}
                     ]
             },
            {:title "Clojurescript"
             :items [{:content "Use the `js` namespace to get things from javascript."
                      :examples [{:content "`(.getElementById js/document \"app\")`"}]
                      }
                     ]
             },
            {:title "Reagent"
             :items [{:content "`:<>` is React fragment (which groups components without enclosing it in an actual element)."}
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


(def data-id (add-ids data nil))

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
                       (reset! current (util/search-list #(= (:id sheet) (:id %)) data-id))))

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
   [everything data-id]
   )

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
