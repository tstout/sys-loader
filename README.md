# Overview
Sys-loader is a tool for spinning up a system of modules written in clojure. In this context the
definition of a module is: 
```
one of a set of parts that can be connected or combined to build or complete something.
```
Clojure’s [cli-deps](https://clojure.org/guides/deps_and_cli) tools provide a compelling environment for combining code stored across multiple git or maven repositories.
A sys-loader module is code stored in a maven or git repository. 
## Design
A module is defined by a module.edn file, typically made available on the classpath as a resource. The EDN file contains a namespace qualified init function and a vector of dependent modules, which are (required), then invoked. The cli-deps tooling makes building up the classpath and invoking configured functions simple. For example, 
```
clojure -M:sys-loader -A:service-1:service-2:service-n
```
This composition of dependencies is a tenet of cli-deps. Simple, powerful, interesting. The term service here is abstract. It is not meant to imply a web service accepting http requests. It could be this, but not necessarily. Each dependency listed after the sys-loader alias given above, is assumed to contain a module.edn resource file containing the init function needed to initialize the module. The EDN file can also contain a list of dependencies. The sys-loader implementation will do a topological sort to invoke the init functions in the appropriate order. 
The cli-deps tooling can be combined with other compatible tools such as [depstar](https://github.com/seancorfield/depstar) to create uberjars for convenient deployment which does not require cli-deps at runtime.

An example module.edn file:
```clojure
[{:sys/description "ring module"
  :sys/name        :ring-module
  :sys/deps        [:sys/logging]
  :sys/init        ring-module.core/init}]
```

Sys-loader provides 
## REPL
A [prepl](https://clojuredocs.org/clojure.core.server/prepl) server is started on port 8000. nRepl is nice, but perhaps initially prepl (built-in) should be used to avoid dependencies. Look here for more info: https://oli.me.uk/clojure-socket-prepl-cookbook/

## Logging
Leaning towards [timbre](https://github.com/ptaoussanis/timbre). Storing logs in a database is useful. By default logs are written to an H2 database.

## Database
An H2 server is provided. 
Most apps/services I have in mind will need several modules/plugins: DB, Logging, pub/sub,  and scheduling. Consider supporting command line options to sys-loader to exclude baked-in modules/plugins from being loaded. The exclusion options are probably not needed. If the module is not listed in any dependency, then it won’t be started.

## Database Migrations (forward only)

# TODO 
## Monitoring
Is JMX sufficient for the beginning? Save this for nice to have, but not necessary.

## Scheduling
Consider providing a standard scheduler perhaps based on chime. This has been working well for years now in [fin-kratzen](https://github.com/tstout/fin-kratzen).
