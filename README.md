# Distributed Cache

It's a task cache, really. See https://github.com/gradle/projects/issues/8 for a description, and the [board](#boards) for the status of the project.

## Benchmarks

See [JMH](http://openjdk.java.net/projects/code-tools/jmh/) benchmark sources in the [`benchmark`](./benchmark) project.

### Hash function performances

These are all using Guava's `HashFunction`s:

![](images/Hash functions Java 8.png)
![](images/Hash functions Java 7.png)

### Hashing strings

Methods used:

* **unencoded** – `HashFunction.hashUnencodedString(string)`
* **default** `HashFunction.hash(string.getBytes())`
* **UTF-8** `HashFunction.hash(string.getBytes(Charsets.UTF8))`
* **UTF-16** `HashFunction.hash(string.getBytes(Charsets.UTF16))`

The **latin** results used 100 latin letters while **unicode** results used 100 random unicode characters.

![](images/MD5 Strings Java 8.png)
![](images/MD5 Strings Java 7.png)
