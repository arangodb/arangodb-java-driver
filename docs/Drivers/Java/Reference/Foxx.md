# Foxx

## call a service

```Java
  Request request = new Request("mydb", RequestType.GET, "/my/foxx/service")
  Response response = arangoDB.execute(request);
```
