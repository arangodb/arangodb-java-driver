# serialization / deserialization
## JavaBeans
The driver can serialize/deserialize JavaBeans. They need at least a constructor without parameter.

``` Java
  public class MyObject {

    private String name;
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## internal fields
To use Arango-internal fields (like _id, _key, _rev, _from, _to) in your JavaBeans, use the annotation `DocumentField`.

``` Java
  public class MyObject {

    @DocumentField(Type.KEY)
    private String key;
    
    private String name;
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## serialized fieldnames
To use a different serialized name for a field, use the annotation `SerializedName`.

``` Java
  public class MyObject {

    @SerializedName("title")
    private String name;
    
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```

## ignore fields
To ignore fields at serialization/deserialization, use the annotation `Expose`

``` Java
  public class MyObject {

    @Expose
    private String name;
    @Expose(serialize = true, deserialize = false)
    private Gender gender;
    private int age;

    public MyObject() {
      super();
    }

  }  
```
