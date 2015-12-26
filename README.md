# clj-github-docs

Generate API docs for Clojure namespaces to include in GitHub repos. The idea
is to generate the API docs and commit to the repo so that documentation can
be versioned with the code it documents.

* Can be used in a [self-contained Boot script][boot-script] to generate
  API docs for namespaces in your project.
* Uses runtime metadata &mdash; no static analysis of files is done.
* Formats API documentation as [GitHub Flavored Markdown][gfmd] with links
  to source for a specific tag or branch in the GitHub repo for the project.
* Supports grouping of vars in namespaces into sections, either via
  metadata on the namespace and vars or externally via macros that add this
  metadata when generating the docs.
* API docs include table of contents, grouped by section if applicable.

## Usage

[](dependency)
```clojure
[adzerk/clj-github-docs "0.1.0"] ;; latest release
```
[](/dependency)

Check out this project's [build.boot](build.boot) file for a simple example.

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
