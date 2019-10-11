# ArangoDB Java Driver

The official ArangoDB Java Driver.

It can be used synchronously as well as asynchronously. The formerly separate async
driver with the same API as the synchronous driver, except that it returned a
`CompletableFuture<T>` instead of the result `T` directly, was merged into this
driver in version 6.2.0. See
[async examples](https://github.com/arangodb/arangodb-java-driver/tree/master/src/test/java/com/arangodb/async/example){:target="_blank"}.

- [Getting Started](GettingStarted/README.md)
- [Reference](Reference/README.md)

## See Also

- [ChangeLog](https://raw.githubusercontent.com/arangodb/arangodb-java-driver/master/ChangeLog.md)
- [Examples](https://github.com/arangodb/arangodb-java-driver/tree/master/src/test/java/com/arangodb/example)
- [Tutorial](https://www.arangodb.com/tutorials/tutorial-sync-java-driver/)
- [JavaDoc](http://arangodb.github.io/arangodb-java-driver/javadoc-6_3/index.html)
- [JavaDoc VelocyPack](http://arangodb.github.io/java-velocypack/javadoc-1_0/index.html)
