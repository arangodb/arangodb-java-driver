# Foxx
##execute a service
``` Java
  Request request = new Request("mydb", RequestType.GET, "/my/foxx/service")
  Response response = arangoDB.execute(request);
 
``` 

##execute a service (async)
``` Java
  Request request = new Request("mydb", RequestType.GET, "/my/foxx/service")
  CompletableFuture<Response> response = arangoDB.executeAsync(request);
 
``` 