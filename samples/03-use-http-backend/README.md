# Using an HTTP backend service

This scenario demonstrates the use of an HTTP cache backend service. You can use this approach to set up a local cache for testing (although you can also use the [built-in local cache implementation](../01-simple-local-caching) if you want to run some simple tests). It is also possible to share the HTTP cache between multiple machines running Gradle builds.

Gradle comes with built-in support for HTTP cache backends.

### Limitations

* The built-in HTTP cache implementation doesn't yet support configuring authentication.

## Preparations

We'll need an HTTP server that is capable of storing files uploaded via `PUT` requests.

### Using Docker

We have a ready-to-use [Docker](https://www.docker.com) container that you can fire up with little extra hassle.

Build the container in the [`http-server`](http-server) directory via:

	$ cd http-server
    $ docker build --tag gradle-task-cache:0.1 .

and run it via:

	$ docker run --name gradle-task-cache -d -p 8885:80 gradle-task-cache:0.1

### Manual setup

If you can't use Docker, you can also install [nginx](https://www.nginx.com) manually. The attached [`nginx.conf`](http-server/nginx.conf) can serve as an example configuration.

You can use any other HTTP server as long as it supports uploading binaries via `PUT`.

## Running builds using the backend

We'll be using the [`init-http.gradle`](http-test/init-http.gradle) init script to configure Gradle to use our HTTP backend. The URL to the cache is passed via the `org.gradle.cache.tasks.http.uri` system property. Note that the URI's path must end in `/`.

In the below examples we are assuming you set up the Docker container locally. If that's not the case make sure the URI points to the HTTP server. You can use `http://username:password@example.com/`-style URIs to specify HTTP Basic credentials.

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


## Troubleshooting

If running from different computers does not have the desired result (i.e. `:compileJava` being in `FROM-CACHE` state), check if the version of Java the same on both computers.

## Keeping a lid on cache size

For running the server for a longer time you may also want to add a cronjob for regularly cleaning up the cache:

```text
0 0 * * * tmpwatch 1d
```
