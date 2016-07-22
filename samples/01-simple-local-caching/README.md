# Simple local caching

```bash
# Empty the local cache directory
rm -rf ~/.gradle/task-cache

# Build project, run application
./gradlew run
```

```text
:compileJava
:run
Hello World!
```

```bash
# Run it again without changes (:compileJava will not be executed)
./gradlew run
```

```bash
# Destroy incremental build data
./gradlew clean

# Re-run (notice that :compileJava is re-executed)
./gradlew run
```

```text
:compileJava
:run
Hello World!
```
