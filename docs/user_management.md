#User Management
If you are using [authentication] (https://docs.arangodb.com/Manual/GettingStarted/Authentication.html) you can manage users with the driver.

##add user
``` Java
  //username, password
  arangoDB.createUser("myUser", "myPassword");
```

##grant user access to database
``` Java
  arangoDB.db("myDatabase").grantAccess("myUser");
````

##revoke user access to database
``` Java
  arangoDB.db("myDatabase").revokeAccess("myUser");
````

##list users
``` Java
  Collection<UserResult> users = arangoDB.getUsers();
  for(UserResult user : users) {
    System.out.println(user.getUser())
  }
```

##delete user
``` Java
  arangoDB.deleteUser("myUser");
```
