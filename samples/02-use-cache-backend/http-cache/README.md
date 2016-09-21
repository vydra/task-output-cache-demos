# Use a http backend for the task output cache

## Setting up the server 

Build the docker container
```
docker build --tag gradle-task-cache:0.1 .

```

and run it via
```
docker run --name gradle-task-cache -d -p 8885:80 gradle-task-cache:0.1
```

Then use the `httpTaskCacheInit.gradle` script for your build and you
should be good to go.

For running it for a longer time you also need to add a cronjob for
cleaning up the cache:

```
0 0 * * * tmpwatch 1d
```