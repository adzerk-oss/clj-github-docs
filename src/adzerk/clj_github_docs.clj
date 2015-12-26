(ns adzerk.clj-github-docs
  "Generate API docs for Clojure namespaces to include in GitHub repos."
  (:require [boot.file        :as file]
            [boot.core        :as boot]
            [clojure.java.io  :as io]
            [clojure.string   :as string :refer [blank? join split trimr]]))

(def ^:private ^:dynamic *doc-ns-sym*)
(def ^:private fixme "FIXME: document this")

(defn- p       [x]   (format "%s\n\n" x))
(defn- hr      [ ]   (p "<hr>"))
(defn- code    [x]   (format "`%s`" x))
(defn- b       [x]   (format "**%s**" x))
(defn- h1      [x]   (p (format "# %s" x)))
(defn- h3      [x]   (p (format "### %s" x)))
(defn- h5      [x]   (p (format "##### %s" x)))
(defn- link    [x y] (format "[%s](%s)" x y))
(defn- block  ([x]   (p (format "```\n%s\n```" x)))
              ([x y] (p (format "```%s\n%s\n```" x y))))

(defn- filter-keys [p m]
  (reduce-kv #(if-not (p %2) %1 (assoc %1 %2 %3)) {} m))

(defn- filter-vals [p m]
  (reduce-kv #(if-not (p %3) %1 (assoc %1 %2 %3)) {} m))

(defn- parse-args [args]
  (->> (partition-all 2 args)
       (split-with (comp keyword? first))
       (map #(apply concat %))
       ((juxt #(apply hash-map (first %)) second))))

(defn- strip-indent [x]
  (let [[head & [z & _ :as tail]] (split (or x fixme) #"\n")
        indent (or (some->> z (re-find #"^ *") count) 0)
        strip  #(try (subs % indent) (catch Throwable _))]
    (->> tail (map strip) (list* head) (join "\n") trimr)))

(defn- fmt-link [sym]
  (-> (str "#" sym)
      (.replaceAll "\\*" "")
      (.replaceAll " " "-")
      (.replaceAll "!" "")))

(defn- emit-arglists [name arglists]
  (some->> arglists
           (sort-by count)
           (map #(pr-str (list* name %)))
           (join " ")
           (block "clojure")))

(defn- emit-var
  [link-path obj {:keys [name doc arglists argspecs file line] :as meta}]
  (let [src-link (format "%s%s#L%d" link-path file line)]
    (str (h3 (link (code name) src-link))
         (emit-arglists name arglists)
         (block (strip-indent doc))
         (hr))))

(defn- emit-vars [sections]
  (->> sections (map :vars) (reduce merge) vals (keep :md)))

(defn- emit-ns [{:keys [name doc]}]
  (remove nil? [(when name (h1 name)) (when doc (p (strip-indent doc)))]))

(defn- emit-toc [xs]
  (->> xs (keep (fn [{:keys [title vars]}]
                  (some->> (map (fn [[x _]] (link (code x) (fmt-link x))) vars)
                           (cons (some-> title h5)) (remove nil?) seq
                           (interpose " ") (apply str) p)))))

(defn- by-section [x]
  (->> (assoc (:doc/toc x) nil {})
       (into (sorted-map))
       (reduce (fn [xs [k v]]
                 (->> (filter-vals #(= k (:doc/section %)) (:vars x))
                      (into (sorted-map)) (assoc v :vars) (conj xs))) [])))

(defn ns-docs
  [link-path ns-sym]
  (let [n (the-ns ns-sym)
        e #(let [m (meta %2)
                 v (var-get (ns-resolve n %1))]
             (assoc m :md (emit-var link-path v m)))
        m (assoc (meta n)
                 :name ns-sym
                 :vars (->> (ns-publics n)
                            (reduce-kv #(assoc %1 %2 (e %2 %3)) {})))
        s (by-section m)]
    (->> [(emit-ns m) (emit-toc s) [(hr)] (emit-vars s)] (reduce concat) (apply str))))

(defn- section* [title & syms]
  (let [id (gensym)
        n  (find-ns *doc-ns-sym*)]
    (alter-meta! n update-in [:doc/toc] assoc id {:title title})
    (doseq [s syms]
      (alter-meta! (ns-resolve n s) assoc :doc/section id))))

(defn generate-docs [ns out-dir src-dir tag ns-doc]
  (let [n   (find-ns ns)
        o   (or out-dir "doc")
        s   (or src-dir "src")
        t   (or tag "master")
        src (format "../%s/%s" t s)
        out (format "%s/%s.md" o ns)]
    (println (format "Writing %s..." out))
    (when ns-doc (alter-meta! n assoc :doc ns-doc))
    (-> out io/file io/make-parents)
    (spit out (-> (.getParent (io/file out))
                  (file/relative-to src)
                  (str "/")
                  (ns-docs ns)))))

(defmacro section [title & syms]
  `(~'section* ~title ~@(map (partial list 'quote) syms)))

(defmacro write-docs [& args]
  (let [[{:keys [ns doc src out tag]} sections] (parse-args args)]
    `(binding [*doc-ns-sym* '~ns]
       (->> (partial #'filter-keys (complement symbol?))
            (alter-meta! (find-ns '~ns) update-in [:doc/toc]))
       (do ~@sections)
       (generate-docs '~ns ~out ~src ~tag ~doc))))
