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
  :sys/deps        []
  :sys/init        ring-module.core/init}]
```
This example has no other sys-loader module deps, thus the empty vector value for :sys/deps.
If a module has dependencies, they would be specified in the :sys/deps vector as the value of the :sys/name key
in the dependency module's declaration.

The init function is a single arg fn. sys-loader will call each module's init function 
with a map containing the following:
```clojure
{:sys/db {:server      #function[sys-loader.db/fn--12770/fn--12779]
          :data-source #object[org.h2.jdbcx.JdbcConnectionPool 0x75208149 "org.h2.jdbcx.JdbcConnectionPool@75208149"]} 
 :sys/migrations #function[clojure.core/partial/fn--5929]}
```
The map at :sys/db contains the following
```
:server - a single arg fn which is a closure around an H2 TCP server instance. The fn accepts the following args:
  :start  - start server listening on TCP port
  :stop   - stop the server from listening
  :server - return the underlying java server object
  :info   - return a map of server details

:data-source - a JDBC DataSource implementation provided by H2's connection pool. 
```
The other key is related to DB migrations

```
:sys/migrations - a fn which accepts a var bound to a fn to execute database DDL.
TODO - need to expand on this with an example.
```

Sys-loader provides 
## REPL Server
A [prepl](https://clojuredocs.org/clojure.core.server/prepl) server is started on port 8000. nRepl is nice, but perhaps initially prepl (provided by clojure) should be used to avoid dependencies. The system property _sys-loader.repl-port_ can be set to override the default prepl port.

A functional editor with PREPL integration can be found at 
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
TODO add more info about this

## Building/Running
There is currently a java class that needs compiling. This is related to configuring log4j2 jdbc logging. To compile the class:
```bash
clojure -T:build compile
```
Note: there is a branch _rm-java-code_ where I have attempted to 
configure log4j2 with clojure code to remove this java
dependency. This currently does not work, and I'm putting it on hold for now.
log4j tries hard to confound a jdbc adapter being configured by anything other than xml files and java code. Apps having sys-loader as a dependency will need to execute 
```bash
clojure -X:deps prep
```
to compile the class.

