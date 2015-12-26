# clj-github-docs

Generate API docs for Clojure namespaces to include in GitHub repos. The idea
is to generate the API docs and commit to the repo so that documentation can
be versioned with the code it documents.

* Can be used in a [Boot script][boot-script] to generate API docs for
  namespaces in your project.
* Uses runtime metadata &mdash; no static analysis of files is done.
* Formats API documentation as [Markdown][gfmd] with links to source for
  a specific tag or branch in the GitHub repo for the project.
* Supports grouping of vars in namespaces into sections, either via
  metadata on the namespace and vars or externally via macros that add this
  metadata when generating the docs.
* API docs include table of contents, grouped by section if applicable.

Check out this project's [build.boot](build.boot) file for a simple example.

## Usage

[](dependency)
```clojure
[adzerk/clj-github-docs "0.1.1"] ;; latest release
```
[](/dependency)

Require both the `clj-github-docs` namespace and the namespaces you wish to
generate docs for:

```clojure
(require
  '[my.docs.namespace]
  '[adzerk.clj-github-docs :refer [write-docs section]])
```

Generate docs using the default settings and metadata on namespace and vars:

```clojure
(write-docs :ns my.docs.namespace)
```

This will write API docs to `doc/my.docs.namespace.md`, without any grouping
of names, assuming the source is in the `src` directory.

The `write-docs` macro accepts a number of keyword arguments which are optional
but must come _before_ the rest of the arguments:

```clojure
(write-docs
  :ns  my.docs.namespace        ; the namespace to document
  :out "doc"                    ; the output directory (will be created)
  :src "src"                    ; the directory containing source files
  :tag "master"                 ; the git branch or tag to use to make links
  :doc "This is my namespace."  ; the namespace docstring to use

  ...                           ; positional arguments (optional), see below
```

### Sections

You can also group vars in a namespace into sections. Sections can be defined
in the namespace as metadata on the namespace and vars:

```clojure
(ns
  ^{:doc "This is my namespace."
    :doc/toc {:01-important  {:title "Very Important"}
              :02-deprecated {:title "Deprecated"}}}
  my.docs.namespace)

(defn ^{:doc/section :01-important
        :doc "Aloha, everyone."}
  say-hello
  []
  (println "hello world"))

(defn ^{:doc/section :02-deprecated
        :doc "Sayonara, people."}
  say-goodbye
  []
  (println "goodbye world"))
```

This establishes a table of contents and associates vars with sections.

> **Note:** The table of contents will be sorted by key.

Generated docs will be grouped by section.

However, sometimes you don't want to add metadata in the namespace source file,
or if you're going to AOT the namespace you'll run into [CLJ-130](http://dev.clojure.org/jira/browse/CLJ-130)
which will cause metadata on the namespace to be lost. Instead of adding the
doc metadata to the namespace and vars you can do this:

```clojure
(write-docs
  :ns  my.docs.namespace
  :doc "This is my namespace."

  (section
    "Very Important"
    say-hello)

  (section
    "Deprecated"
    say-goodbye))
```

These macros add the metadata dynamically at runtime.

## Hacking

```
# build and install locally
boot build-jar
```
```
# push snapshot
boot build-jar push-snapshot
```
```
# push release
boot build-jar push-release
```

## License

Copyright © 2015 Adzerk

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[boot-script]: https://github.com/boot-clj/boot/wiki/Scripts
[gfmd]: https://help.github.com/articles/github-flavored-markdown/
