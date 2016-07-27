# Using a cache backend service

This scenario demonstrates the use of a cache backend service. The first part of the scenario demonstrates using the backend service locally. The second part shows how it works when the backend is accessed from builds running on different hosts.

We are going to use a [Hazelcast](http://hazelcast.org) node as the cache backend. An [init-script plugin](https://docs.gradle.org/current/userguide/init_scripts.html#N14C1D) implements the connection between the Gradle build tool and the Hazelcast node. This init-script plugin is not part of the Gradle distribution, but is a standalone plugin that lives in the [`gradle-hazelcast-plugin` repository](https://github.com/lptr/gradle-hazelcast-plugin). It also serves as the reference implementation for Gradle cache backend support. Plugins supporting other backends (like Redis, Varnish etc.) can be created in a similar way by implementing the `TaskOutputCacheFactory` interface like [it is done in the Hazelcast plugin](https://github.com/lptr/gradle-hazelcast-plugin/blob/6f1c5ab64e6d9cad2a15fda26d994e4e07d9a51c/src/main/java/org/gradle/cache/tasks/hazelcast/HazelcastPlugin.java).

Hazelcast itself is an in-memory data store, so it will only keep track of the cached data as long as the Hazelcast node itself is running. This makes it easy to discard the cached data when needed by restarting the node. Hazelcast can work as a distributed cache with nodes talking to each other. For this scenario however we are going to create a centralized cache service with a single standalone node.

### Limitations

At this point in the development of the task output cache feature it has the following limitations:

* One of the main points of using the same cache backend with builds executed on different hosts is that one build can reuse the results from builds that were executed other machines. For now this will only work if the input file paths are exactly the same on each machine. This practically means that if you ran the build in the `/Users/lptr/Workspace/gradle-task-cache` directory on host A, you'll need to run it from the exact same directory on host B as well for caching to work. This limitation will be lifted once [issue #18](https://github.com/gradle/task-output-cache/issues/18) is implemented.
* The only task types that are cached by default are `JavaCompile` and `Jar`. More tasks are going to be made cached later, focusing on the Java toolchain first. If needed, caching for a task can be turned on via `TaskOutputs.cacheIf { true }`.
* Difference in the Java toolchain being used to run the compilation is not recognized at this point. (See [issue #39](https://github.com/gradle/task-output-cache/issues/39).)


## Preparations

We first need the standalone Hazelcast node to be up and running. For this, let's build the `hazelcast-server` tool first:

```text
$ cd hazelcast-server
$ ./gradlew installDist
:compileJava
:compileGroovy UP-TO-DATE
:processResources UP-TO-DATE
:classes
:jar
:startScripts
:installDist
```

Now we can fire up the Hazelcast node:

```text
$ build/install/hazelcast-server/bin/hazelcast-server run
Jul 27, 2016 1:17:30 PM com.hazelcast.instance.Node
WARNING: [192.168.1.7]:5701 [dev] [3.6.4] No join method is enabled! Starting standalone.
```

We are now ready to use the cache!

## Testing locally

Let's first clean everything:

```text
$ cd ..
$ cd hazelcast-test
$ ./gradlew clean
:clean
```

The configuration for task caching now happens in an init-script under [`hazelcast-test/init-hazelcast.gradle`](hazelcast-test/init-hazelcast.gradle). It applies the [`gradle-hazelcast-plugin`](https://github.com/lptr/gradle-hazelcast-plugin) which enables caching via a Hazelcast backend.

Let's run the build on the same machine. The default settings should work fine:

```text
$ /gradlew -Dorg.gradle.cache.tasks=true -I init-hazelcast.gradle run
Task output caching is an incubating feature.
:compileJava
:processResources UP-TO-DATE
:classes
:run
Hello World!
```

At this time, the results of `:compileJava` and `:jar` were stored in the cache.

Let's remove the build results again:

```text
$ ./gradlew clean
:clean
```

**Note:** We need to do this, because if we don't, the next time we run the build, the incremental build feature will detect that the tasks are already `UP-TO-DATE`, and we'll not load their results from the cache.

So once we took care of removing the previous build outputs, we can run the build again:

```text
$ ./gradlew -Dorg.gradle.cache.tasks=true -I init-hazelcast.gradle run
Task output caching is an incubating feature.
:compileJava CACHED
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
Hello World!
```


## Using the cache from a different host

It is possible to specify the Hazelcast node's host via `org.gradle.cache.tasks.hazelcast.host`. The `org.gradle.cache.tasks.hazelcast.port` property can similarly be used to specify the TCP port the Hazelcast server is running on. (When started, the Hazelcast server will print its IP address.)

Let's run the same build from a different machine:

```text
$ ./gradlew -Dorg.gradle.cache.tasks=true -Dorg.gradle.cache.tasks.hazelcast.host=192.168.1.7 -I init-hazelcast.gradle run
Task output caching is an incubating feature.
:compileJava CACHED
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
Hello World!
```

### Changing the Hazelcast TCP port

It is also possible to set the TCP port used explicitly (the default is `5701`):

```text
$ hazelcast-server/build/install/hazelcast-server/bin/hazelcast-server run --port 5710
Jul 27, 2016 1:55:58 PM com.hazelcast.instance.Node
WARNING: [192.168.1.7]:5710 [dev] [3.6.4] No join method is enabled! Starting standalone.
```

The server tool has some more options:

```text
$ hazelcast-server/build/install/hazelcast-server/bin/hazelcast-server help run
NAME
        hazelcast-server run - run a Hazelcast server

SYNOPSIS
        hazelcast-server run [(-d | --debug)] [(-M | --enable-multicast)]
                [(-p <port> | --port <port>)] [(-q | --quiet)] [(-v | --verbose)]

OPTIONS
        -d, --debug
            debug mode

        -M, --enable-multicast
            enable multicast discovery

        -p <port>, --port <port>
            port to start the server on, defaults to 5701

        -q, --quiet
            quiet mode, only print errors

        -v, --verbose
            verbose mode
```
