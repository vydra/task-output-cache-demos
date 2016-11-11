# Using an HTTP backend service

This scenario demonstrates the use of an HTTP cache backend service. You can use this approach to set up a local cache for testing (although you can also use the [built-in local cache implementation](../01-simple-local-caching) if you want to run some simple tests). It is also possible to share the HTTP cache between multiple machines running Gradle builds.

Gradle comes with built-in support for HTTP cache backends.

### Limitations

* The built-in HTTP cache implementation doesn't yet support authentication. You can use the `http://username:passowrd@example.com` URL format to supply HTTP basic credentials if needed.

## Preparations

### HTTP server

We'll need an HTTP server that is capable of storing files uploaded via `PUT` requests.

#### Using Docker

We have a ready-to-use [Docker](https://www.docker.com) container that you can fire up with little extra hassle.

Build the container in the [`http-server`](http-server) directory via:

	$ cd http-server
    $ docker build --tag gradle-task-cache:0.1 .

and run it via:

	$ docker run --name gradle-task-cache -d -p 8885:80 gradle-task-cache:0.1

#### Manual setup

If you can't use Docker, you can also install [nginx](https://www.nginx.com) manually. The attached [`nginx.conf`](http-server/nginx.conf) can serve as an example configuration.

You can use any other HTTP server as long as it supports uploading binaries via `PUT`.

### Keeping a lid on cache size

For running the server for a longer time you may also want to add a cronjob for regularly cleaning up the cache:

```text
0 0 * * * tmpwatch 1d
```

## Running builds using the backend

We'll be using 

```text
$ cd http-test
$ ./gradlew -Dorg.gradle.cache.tasks=true -I init-http.gradle -Dorg.gradle.cache.tasks.http.uri=http://localhost:8885/ clean run
Task output caching is an incubating feature.
:clean
:compileJava
:processResources UP-TO-DATE
:classes
:run
Hello World!
```

At this time, the results of `:compileJava` and `:jar` were stored in the cache.

**Note:** We'll be running every build with the `clean` task included. If the build has already been executed, this removes any output present in the `build` directory. Had we not done this, the incremental build feature in Gradle could mark some tasks as `UP-TO-DATE`, which in turn would prevent the new task output cache feature to kick in.

Let's run the build again:

```text
$ ./gradlew -Dorg.gradle.cache.tasks=true -I init-http.gradle -Dorg.gradle.cache.tasks.http.uri=http://localhost:8885/ clean run
Task output caching is an incubating feature.
:clean
:compileJava FROM-CACHE
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
Hello World!
```

Notice how `:compileJava` is now `FROM-CACHE`.


## Using the cache from a different host

Let's try to use the stored results from another computer. We're going to specify the Hazelcast node's host via `org.gradle.cache.tasks.hazelcast.host`. When started, the Hazelcast server will print its IP address.

Let's run the same build from a different machine:

```text
$ ./gradlew -Dorg.gradle.cache.tasks=true -Dorg.gradle.cache.tasks.hazelcast.host=192.168.1.7 -I init-hazelcast.gradle clean run
Task output caching is an incubating feature.
:clean
:compileJava FROM-CACHE
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
Hello World!
```

### Troubleshooting

If running from different computers does not have the desired result (i.e. `:compileJava` being in `FROM-CACHE` state), check if the version of Java the same on both computers.

### Changing the Hazelcast TCP port

It is also possible to set the TCP port used explicitly via the `org.gradle.cache.tasks.hazelcast.port` property (the default is `5701`):

```text
$ hazelcast-server/build/install/hazelcast-server/bin/hazelcast-server run --port 5710
Jul 27, 2016 1:55:58 PM com.hazelcast.instance.Node
WARNING: [192.168.1.7]:5710 [dev] [3.6.4] No join method is enabled! Starting standalone.
```

### More about the Hazelcast server tool

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
