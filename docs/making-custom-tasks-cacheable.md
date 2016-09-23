# Making custom tasks cacheable

Gradle already provides a number of tasks that are cached by default. It is easy to make custom tasks cacheable as well, as long as they meet a set of criteria.

## Enabling caching for a task

The preferred way to make tasks cacheable is to mark the task class with `@CacheableTask`. This will tell Gradle to try to use caching for every instance of the task type by default.

Annotating the task type is not an option for ad-hoc tasks, and sometimes it is not possible to modify the task type. In some cases it is desirable to base the cacheablility of a task on some program logic. For these cases caching can be configured via `TaskOutputs.cacheIf()` as well. It is possible to call this method on `@CacheableTask`s as well; in which case the annotation is ignored.

**Note:** The `@CacheableTask` annotation is _not_ inherited, so if you want a sub-type of the task to be cacheable, you'll need to also annotate the sub-type.

## Requirements for cacheable tasks

The importance of declaring all inputs of a cacheable task and declaring them correctly is paramount. It's much more important to be correct than it was with simple incremental builds. If the incremental build produced a false positive (i.e. identified a task to be `UP-TO-DATE` when it wasn't), the user could simply run the build again with `clean`. However, if we identify cached results to match the inputs of a task when in reality the task works with some different inputs, this can lead to reusing the wrong results, and ultimately producing corrupt builds.

To a lesser degree false negatives (i.e. not recognizing a cached result that would be perfectly usable instead of executing a task) can lead to performance problems. On one hand we'd be executing more tasks than necessary, on the other we'd be storing the results redundantly, wasting time, bandwidth and storage space.

### Relaxing input path sensitivity

The reuse of cached results depends on whether or not we can recognize a hit. Gradle by default takes the full path of input files into account when considering a task. This means that by default a task's results can only be reused if the absolute path of the inputs match. This means cached results will only be reused in the same local Gradle project. To enable sharing results between hosts each host must run the builds from the exact same path.

To relax this requirement, a task can declare its input's _path sensitivity._ This basically allows Gradle to ignore parts or all of an input file's path when considering the task state, and ultimately generating the cache key.

For many tasks this can be appropriate, as we only need to consider the parts of a file's path that actually has an effect on the task's output.

```java
@PathSensitive(PathSensitivity.NAME_ONLY)
@InputFiles
public FileTree getSources() {
	// ...
}
```

Given a set of inputs consisting of one or more file hierarchies, Gradle supports the following strategies of handling files being moved:

* `@PathSensitive(PathSensitivity.ABSOLUTE)` – this is the default behavior: any of the files is moved to a different path is considered as a change of the task's inputs.
* `@PathSensitive(PathSensitivity.RELATIVE)` – the location of the file hierarchies on disk are ignored. Only when a file is moved relative to its containing hierarchy do we consider the inputs changed. One example is an ANTLR grammar where the location of the input file is important, because the generated files will be placed in a similar directory hierarchy.
* `@PathSensitive(PathSensitivity.NAME_ONLY)` – the paths of the files are ignored, and only files being renamed are considered as changes to the task's inputs. Java sources are treated like this in the `JavaCompile` task.
* `@PathSensitive(PathSensitivity.NONE)` – the path of name of any of the input files can change freely without the task's input being considered different. This setting is ideal for things like configuration files where only the contents of the file matter.
* `@Classpath` – this is specifically for Java classpath inputs. It's similar to how `RELATIVE` works, but it ignores file names for files added directly to the classpath.


### Specifying outputs

A cacheable task should have declared outputs, either via `@OutputFile` and `@OutputDirectory` properties, or by calling `TaskOutputs.file()` and `dir()`.

**Note:** We don't yet support caching tasks with `@Optional` (see [#81](https://github.com/gradle/task-output-cache/issues/81)) or plural outputs (see [#57](https://github.com/gradle/task-output-cache/issues/57)).

## Troubleshooting

If a task doesn't get loaded from cache when in theory it should, there's a couple of things one can do. Running the build with `--info` will display the cache key generated for each task. Using `--debug` also gives information about what information is used to generate the key:

```text
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.execution.SkipCachedTaskExecuter] Determining if task ':jar' is cached already
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: org.gradle.api.tasks.bundling.Jar_Decorated
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending bytes to cache key: 27:89:cb:1e:7b:d7:a3:7e:23:c4:aa:32:12:31:c2:25
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending bytes to cache key: 79:80:c2:22:82:96:b7:bd:e5:6e:1a:17:15:c5:74:c1
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: entryCompression
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: org.gradle.api.tasks.bundling.ZipEntryCompression
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: DEFLATED
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: manifestContentCharset
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: UTF-8
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: metadataCharset
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: UTF-8
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.caseSensitive
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: true
15:49:57.229 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.destPath
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key:
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.dirMode
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: $NULL
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.duplicatesStrategy
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: org.gradle.api.file.DuplicatesStrategy
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: INCLUDE
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.fileMode
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: $NULL
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.filteringCharset
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: UTF-8
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1.includeEmptyDirs
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: true
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.caseSensitive
15:49:57.230 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: true
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.destPath
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: META-INF
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.dirMode
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: $NULL
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.duplicatesStrategy
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: org.gradle.api.file.DuplicatesStrategy
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: INCLUDE
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.fileMode
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: $NULL
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.filteringCharset
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: UTF-8
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1.includeEmptyDirs
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: true
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.caseSensitive
15:49:57.231 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: true
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.destPath
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: META-INF
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.dirMode
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: $NULL
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.duplicatesStrategy
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: org.gradle.api.file.DuplicatesStrategy
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: INCLUDE
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.fileMode
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: $NULL
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.filteringCharset
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: UTF-8
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2.includeEmptyDirs
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: true
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: zip64
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending boolean to cache key: false
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$1
15:49:57.232 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: Hello.class
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending bytes to cache key: 0e:40:02:9b:76:2e:76:00:6e:ea:4a:05:0b:28:32:79
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: resource.properties
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending bytes to cache key: c7:05:0e:12:05:6d:29:39:5c:1e:4c:63:1e:6b:59:c5
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: rootSpec$2$1
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending string to cache key: MANIFEST.MF
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Appending bytes to cache key: 12:33:05:d4:e8:0b:67:4b:6e:86:1c:ea:db:40:c6:7b
15:49:57.233 [DEBUG] [org.gradle.api.internal.tasks.cache.DefaultTaskCacheKeyBuilder] Hash code generated: 3c328c0550b86de6029a7dafaaa56d85
15:49:57.233 [INFO] [org.gradle.api.internal.tasks.execution.SkipCachedTaskExecuter] Cache key for task ':jar' is 3c328c0550b86de6029a7dafaaa56d85
```
