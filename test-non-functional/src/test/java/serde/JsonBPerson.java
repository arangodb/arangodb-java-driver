//package serde;
//
//
//import com.arangodb.serde.jsonb.Key;
//import jakarta.json.bind.annotation.JsonbProperty;
//
//import java.util.Objects;
//
//public class JsonBPerson {
//    @Key
//    private String key;
//    @JsonbProperty("firstName")
//    private String name;
//    private int age;
//
//    public JsonBPerson() {
//    }
//
//    public JsonBPerson(String key, String name, int age) {
//        this.key = key;
//        this.name = name;
//        this.age = age;
//    }
//
//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        JsonBPerson person = (JsonBPerson) o;
//        return age == person.age && Objects.equals(key, person.key) && Objects.equals(name, person.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(key, name, age);
//    }
//}