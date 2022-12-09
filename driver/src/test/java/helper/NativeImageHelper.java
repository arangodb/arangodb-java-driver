package helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helper scripts to generate GraalVM native image configuration
 *
 * @author Michele Rastelli
 */
public class NativeImageHelper {
    public static void main(String[] args) throws JsonProcessingException {
        generateReflectConfig();
    }

    private static void generateReflectConfig() throws JsonProcessingException {
        System.out.println("---------------------------");
        System.out.println("--- reflect-config.json ---");
        System.out.println("---------------------------");

        List<String> packages = Arrays.asList(
                "com.arangodb.entity",
                "com.arangodb.model",
                "com.arangodb.internal.cursor.entity"
        );

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode rootNode = mapper.createArrayNode();

        String internalSerdePackage = "com.arangodb.internal.serde";
        Collection<URL> serdeUrls = ClasspathHelper.forPackage(internalSerdePackage);
        Reflections r = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false))
                .setUrls(serdeUrls)
                .filterInputsBy(new FilterBuilder().includePackage(internalSerdePackage)));
        Stream<String> serializers = r.getSubTypesOf(JsonSerializer.class).stream()
                .filter(it -> !it.isAnonymousClass())
                .map(Class::getName);
        Stream<String> deserializers = r.getSubTypesOf(JsonDeserializer.class).stream()
                .filter(it -> !it.isAnonymousClass())
                .map(Class::getName);
        Stream<String> serdeClasses = Stream.concat(serializers, deserializers)
                .filter(it -> it.contains("InternalSerializers") || it.contains("InternalDeserializers"));

        Stream<String> entityClasses = packages.stream()
                .flatMap(p -> {
                    final ConfigurationBuilder config = new ConfigurationBuilder()
                            .setScanners(new SubTypesScanner(false))
                            .setUrls(ClasspathHelper.forPackage(p))
                            .filterInputsBy(new FilterBuilder().includePackage(p));

                    Reflections reflections = new Reflections(config);
                    return Stream.concat(
                            reflections.getAllTypes().stream(),
                            reflections
                                    .getSubTypesOf(Enum.class)
                                    .stream()
                                    .map(Class::getName)
                    );
                });
        Stream.concat(serdeClasses, entityClasses)
                .filter(className -> className.startsWith("com.arangodb"))
                .map(className -> {
                    ObjectNode entry = mapper.createObjectNode();
                    entry.put("name", className);
                    entry.put("allDeclaredFields", true);
                    entry.put("allDeclaredMethods", true);
                    entry.put("allDeclaredConstructors", true);
                    return entry;
                })
                .forEach(rootNode::add);

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        System.out.println(jsonString);
    }
}
