# Diagnosing the task cache

In order to get detailed diagnostics about the cache consider adding the following init script to your build:

```groovy
apply from: 'https://raw.githubusercontent.com/gradle/gradle/master/gradle/taskCacheDetailedDiagnosticsInit.gradle'
```

This will add the following diagnostics.

### Detailed cache statistics
   
This displays which tasks have been cached and which not by type. It also shows the time used.

<details>
<summary>Example</summary>

```
Detailed cache statistics

  All tasks - 78 tasks took 3103 ms (avg 39,78 ms, stddev 288,61 ms, min 0 ms, max 2566 ms)
    FROM_CACHE - 23 tasks took 410 ms (avg 17,83 ms, stddev 33,02 ms, min 1 ms, max 152 ms)
      Cacheable - 23 tasks took 410 ms (avg 17,83 ms, stddev 33,02 ms, min 1 ms, max 152 ms)
        org.gradle.api.tasks.bundling.Jar - 1 task took 2 ms
        org.gradle.api.tasks.compile.JavaCompile - 9 tasks took 364 ms (avg 40,44 ms, stddev 44,04 ms, min 6 ms, max 152 ms)
        org.gradle.build.BuildReceipt - 1 task took 1 ms
        org.gradle.build.ClasspathManifest - 12 tasks took 43 ms (avg 3,58 ms, stddev 1,98 ms, min 1 ms, max 8 ms)
    UP_TO_DATE - 50 tasks took 99 ms (avg 1,98 ms, stddev 7,56 ms, min 0 ms, max 54 ms)
      Not cacheable - 50 tasks took 99 ms (avg 1,98 ms, stddev 7,56 ms, min 0 ms, max 54 ms)
        org.gradle.api.DefaultTask - 12 tasks took 56 ms (avg 4,67 ms, stddev 14,88 ms, min 0 ms, max 54 ms)
        org.gradle.api.tasks.bundling.Jar - 11 tasks took 27 ms (avg 2,45 ms, stddev 1,30 ms, min 1 ms, max 5 ms)
        org.gradle.api.tasks.compile.GroovyCompile - 12 tasks took 3 ms (avg 0,25 ms, stddev 0,43 ms, min 0 ms, max 1 ms)
        org.gradle.api.tasks.compile.JavaCompile - 3 tasks took 8 ms (avg 2,67 ms, stddev 2,49 ms, min 0 ms, max 6 ms)
        org.gradle.language.jvm.tasks.ProcessResources - 12 tasks took 5 ms (avg 0,42 ms, stddev 0,49 ms, min 0 ms, max 1 ms)
    EXECUTED - 5 tasks took 2594 ms (avg 518,80 ms, stddev 1023,65 ms, min 0 ms, max 2566 ms)
      Not cacheable - 4 tasks took 28 ms (avg 7,00 ms, stddev 11,00 ms, min 0 ms, max 26 ms)
        org.gradle.api.DefaultTask - 3 tasks took 26 ms (avg 8,67 ms, stddev 12,26 ms, min 0 ms, max 26 ms)
        org.gradle.api.tasks.Copy - 1 task took 2 ms
      Cacheable - 1 task took 2566 ms
        org.gradle.api.tasks.compile.JavaCompile - 1 task took 2566 ms
```
</details>

The output can also be formatted as csv by adding `-Dcsv` to the Gradle command line.
The csv can be written to a file by using `-Dcsv=<file name>`.
 
### Overlapping output directories

We detect overlapping output directories since these can be a problem for caching.
We can only detect overlapping output directories for tasks which are being executed.
The information will be printed at the end of the build.

### Publish inputs and outputs of tasks to Gradle Enterprise

If a build scan is generated we publish some basic statistics about the cache to Gradle Enterprise.
We use custom values for that. In addition it is possible to publish the hashes of the inputs and outputs of a task.
Pass `-Pcache.investigate.tasks=<task path>` to the Gradle build (e.g. `-P:core:compileJava`). The property can also
be set to a comma separated list of task paths.

To obtain this data as JSON you can use the following url: `<Gradle enterprise server>/scan-data/<scan id>/custom-values`, e.g. [https://e.grdev.net/scan-data/6ciys7ufpomni/custom-values](https://e.grdev.net/scan-data/6ciys7ufpomni/custom-values).