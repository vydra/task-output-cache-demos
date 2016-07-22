# Simple local caching

This is a test scenario that demonstrates the difference between using the new task output caching vs. relying only on the existing incremental builds feature.

## With caching disabled

Build project, run application:

```text
$ ./gradlew run
:compileJava
:processResources UP-TO-DATE
:classes
:run
Hello World!
```

Run it again without changes (`:compileJava` will not be executed):

```text
$ ./gradlew run
:compileJava UP-TO-DATE
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
Hello World!
```

Destroy incremental build data

```text
$ ./gradlew clean
:clean
```

Re-run again (notice that `:compileJava` is re-executed):

```text
$ ./gradlew run
:compileJava
:processResources UP-TO-DATE
:classes
:run
Hello World!
```

## With cache enabled

Empty the local cache directory:

```text
$ rm -rf ~/.gradle/task-cache
```

Destroy incremental build state:

```text
$ ./gradlew clean
:clean
```

Build project with cache enabled, run application:

```text
$ ./gradlew -Dorg.gradle.cache.tasks=true run
Task output caching is an incubating feature.
:compileJava
:processResources UP-TO-DATE
:classes
:run
Hello World!
```

Works just as before. But let's kill incremental build state again:

```text
$ ./gradlew clean
:clean
```

And if we rebuild again with cache enabled:

```text
$ ./gradlew -Dorg.gradle.cache.tasks=true run
Task output caching is an incubating feature.
:compileJava CACHED
:processResources UP-TO-DATE
:classes UP-TO-DATE
:run
Hello World!
```

Notice that `:compileJava` is now `CACHED`, i.e. it is loaded from the cache.

Check the contents of the cache:

```text
$ ls ~/.gradle/task-cache/
e19b127fa94b0ee3e1464408dec0ddfd

$ unzip -l ~/.gradle/task-cache/e19b127fa94b0ee3e1464408dec0ddfd
Archive:  ~/.gradle/task-cache/e19b127fa94b0ee3e1464408dec0ddfd
  Length     Date   Time    Name
 --------    ----   ----    ----
        0  07-22-16 22:43   property-dependencyCacheDir/
        0  07-22-16 22:43   property-destinationDir/
      519  07-22-16 22:43   property-destinationDir/Hello.class
 --------                   -------
      519                   3 files
```
