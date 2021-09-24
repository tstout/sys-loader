# Overview
This is essentially some nostalgia for something I was trying to achieve with [splumb](https://github.com/tstout/splumb).  I’m leaning now toward something useful that can spin up a system of components, modules, plugins, etc. I have not developed a name for the base unit that I like. However, clojure makes the plugin thing easy.
Clojure’s [cli-deps](https://clojure.org/guides/deps_and_cli) tools provide a simple, compelling environment for combining “components” stored across multiple git or maven repositories.
## Design
A plugin.edn file is loaded as a resource. The EDN file contains a qualified init function and a vector of dependent plugins (services/modules/components)  which is required, then invoked. The cli-deps tooling makes building up the classpath and invoking configured functions simple. For example, 
```
clj -M:sys-loader:service-1:service-2:service-n
```
This composition of dependencies is a fundamental design of cli-deps. Simple, powerful, interesting. The clojure way. The term service here is abstract. It is not meant to imply a typical web service accepting http requests. It could be this, but not necessarily. Each dependency listed after the sys-loader alias given above, contains a plugin.edn resource file containing the init function needed to initialize the component. The edn file can also contain a list of dependencies. The sys-loader implementation will do a topological sort to invoke the init functions in the appropriate order. 
## IPC
A socket API may evolve to send control and status requests to the sys-loader jvm. However, for starters, a simpler memory-mapped file supported by NIO is likely sufficient. This will support A CLI to send commands to the loader. A CLI will be the primary user interface. Spinning up a webserver to provide a UI might be a little heavy-weight.

A REPL connection will likely suffice for this functionality. nRepl is nice, but perhaps initially prepl (built-in) should be used to avoid dependencies. Look here for more info: https://oli.me.uk/clojure-socket-prepl-cookbook/

## Lifecycle
Lifecycles for a plugin could be useful. More thought is needed on this. Restarting the entire process is likely good enough for now. Supporting a lifecycle can complicate things. 

## Logging
Leaning towards [timbre](https://github.com/ptaoussanis/timbre). Storing logs in a DB is useful. By default logs will be written to H2.
## Monitoring
Is JMX sufficient for the beginning? Save this for nice to have, but not necessary.

## Configuration
H2 Server will provide access to per module/plugin configuration. Each module/plugin will have an EDN blob. Each module’s config will be loaded at startup, and provided to it’s init function. This will allow config editing using a standard SQL client (like DBeaver). Functions can also be provided via REPL to update configuration. No hot-reload in the beginning. A module/plugin’s init function can return a map containing any resource handles that might be needed by its dependents. The configuration provided to the init function will be enriched with any dependent handle information, such as DB connections. Plugin/module edn config file can contain an optional map of configuration items. This will be used to seed the initial values stored in the DB.
_Update_ Upong further reflection, leave config out of this for now.

Storing config in git repo might be useful here as well?

## Scheduling
Consider providing a standard scheduler perhaps based on chime. This has been working well for years now in [fin-kratzen](https://github.com/tstout/fin-kratzen).

## Database
An H2 server is provided. Currently used to store logs. 
Most apps/services I have in mind will need several modules/plugins: DB, Logging, pub/sub,  and scheduling. Consider supporting command line options to sys-loader to exclude baked-in modules/plugins from being loaded. The exclusion options are probably not needed. If the module is not listed in any dependency, then it won’t be started.
