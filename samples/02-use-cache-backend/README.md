# Using a cache backend service

This scenario demonstrates the use of a cache backned service. The tests can be ran locally, and they can also be used to demonstrate the use of the cache backend by builds running on different hosts.

We are going to use a [Hazelcast](http://hazelcast.org) node as the cache backend. This backend serves as the reference implementation for other cache backend implementations, and it can be found in the [`gradle-hazelcast-plugin` repository](https://github.com/lptr/gradle-hazelcast-plugin). This is an [init-script plugin](https://docs.gradle.org/current/userguide/init_scripts.html#N14C1D) that is not part of the Gradle distribution. Plugins supporting other backends can be created in a similar way by implementing the [`TaskOutputCacheFactory` interface](https://github.com/gradle/gradle/blob/b78f935c8c18ec4ea695049d3697025ba3a945e2/subprojects/core/src/main/java/org/gradle/api/internal/tasks/cache/TaskOutputCacheFactory.java).

Hazelcast is an in-memory database, so it will only keep track of the cached data as long as the Hazelcast node is running. This makes it easy to discard the cached data when needed by restarting the node. Hazelcast can work as a distributed cache with nodes talking to each other. For this test however we are going to create a centralized cache service with a single standalone node.

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
