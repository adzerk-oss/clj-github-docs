(set-env!
  :resource-paths #{"src"}
  :dependencies   '[[org.clojure/clojure "1.7.0"  :scope "provided"]
                    [adzerk/bootlaces    "0.1.13" :scope "test"]])

(require
  '[adzerk.bootlaces        :refer :all]
  '[adzerk.clj-github-docs  :refer :all])

(def +version+ "0.1.0")

(bootlaces! +version+)

(task-options!
  pom  {:project     'adzerk/clj-github-docs
        :version     +version+
        :description "Generate API docs for Clojure namespaces to include in GitHub repos."
        :url         "https://github.com/adzerk-oss/clj-github-docs"
        :scm         {:url "https://github.com/adzerk-oss/clj-github-docs"}
        :license     {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask api-docs
  []
  (with-pass-thru [_]
    (write-docs :ns adzerk.clj-github-docs :tag +version+)))
