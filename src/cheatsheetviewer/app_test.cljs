(ns cheatsheetviewer.app-test
  (:require
            [cljs.test :refer (deftest is run-tests)]
            ))

(deftest test-numbers
  (is (= 1 1)))

(run-tests)
