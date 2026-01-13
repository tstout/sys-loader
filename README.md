## Usage - deps.edn coordinates
```clojure
  com.github.tstout/sys-loader
    {:git/url "https://github.com/tstout/sys-loader"
     :git/tag "v1.1.7"
     :git/sha "4e1ce47"}
```

# Overview
Sys-loader is a tool for spinning up a system of modules written in clojure. In this context the
definition of a module is: 
```
one of a set of parts that can be connected or combined to build or complete something.
```
Clojure’s [cli-deps](https://clojure.org/guides/deps_and_cli) tools provide a compelling environment for combining code stored across multiple git or maven repositories.
A sys-loader module is code stored in a maven or git repository. 

## Design
A module is defined by a module.edn file made available on the classpath as a resource. The EDN file contains a namespace qualified init function and a vector of dependent modules, which are (required), then invoked. The cli-deps tooling makes building up the classpath and invoking configured functions simple. For example, 
```
clojure -M:sys-loader -A:service-1:service-2:service-n
```
This composition of dependencies is a tenet of cli-deps. Simple, powerful, interesting. The term service here is abstract. It is not meant to imply a web service accepting http requests. It could be this, but not necessarily. Each dependency listed after the sys-loader alias given above, is assumed to contain a module.edn resource file containing the init function needed to initialize the module. The EDN file can also contain a list of dependencies. The sys-loader implementation will do a topological sort to invoke the init functions in the appropriate order. 
The cli-deps tooling can create uberjars for convenient deployment which does not require cli-deps at runtime.

An example module.edn file:
```clojure
[{:sys/description "ring module"
  :sys/name        :ring-module
  :sys/init        ring-module.core/init}]
```
The init function is a single arg fn. sys-loader will call the init functions 
with a map containing the following:
```clojure
{:sys/db {:server      H2 Server instance
          :data-source Corresponding H2 datasource}
 :sys/migrations migration-fn}
```
Sys-loader provides 
## REPL
A [prepl](https://clojuredocs.org/clojure.core.server/prepl) server is started on port 8000. nRepl is nice, but perhaps initially prepl (provided by clojure) should be used to avoid dependencies. Look here for more info: https://oli.me.uk/clojure-socket-prepl-cookbook/
The system property _sys-loader.repl-port_ can be set to override the default prepl port.

A functional Editor with PREPL integration can be found at 
[repl-kit](https://github.com/tstout/repl-kit)

## Database
An H2 server is started automatically on port 9092.
The system property _sys-loader.h2-port_ can be set to override the default port.
The following jdbc connection string can be used to connect to the H2 DB
```
jdbc:h2:tcp://localhost:9092/~/.sys-loader/db/sys-loader;jmx=true
```
## Logging
Storing logs in a database is useful. By default logs are written to an H2 database. [tools.logging](https://github.com/clojure/tools.logging) with log4j2 providing the logging implementation. Not really happy with this, but have not found a better
alternative to play nice with the java ecosystem. Logs can be found in the table *SYS_LOADER.EVENT_LOGS*.

## Database Migrations (forward only)


## Building/Running
There is currently a java class that needs compiling. This is related to configuring log4j2 jdbc logging. To compile the class:
```bash
clojure -T:build compile
```
Note: there is a branch _rm-java-code_ where I have attempted to 
configure log4j2 with clojure code to remove this java
dependency. This currently does not work, and I'm putting it on hold for now.
log4j tries hard to confound being configured by anything other than xml files.
Apps having sys-loader as a dependency will need to execute 
```bash
clojure -X:deps prep
```
to compile the class.

# TODO 
## Monitoring
Is JMX sufficient for the beginning? Save this for nice to have, but not necessary.

## Scheduling
Consider providing a standard scheduler perhaps based on chime. This has been working well for years now in [fin-kratzen](https://github.com/tstout/fin-kratzen).
