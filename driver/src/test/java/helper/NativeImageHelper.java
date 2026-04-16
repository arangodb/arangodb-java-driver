package helper;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helper scripts to generate GraalVM native image configuration
 *
 * @author Michele Rastelli
 */
public class NativeImageHelper {
    public static void main(String[] args) {
        generateReflectConfig();
    }

    private static void generateReflectConfig() {
        System.out.println("---------------------------");
        System.out.println("--- reflect-config.json ---");
        System.out.println("---------------------------");

        List<String> packages = Arrays.asList(
                "com.arangodb.entity",
                "com.arangodb.model",
                "com.arangodb.internal.cursor.entity"
        );

        JsonMapper mapper = new JsonMapper();
        ArrayNode rootNode = mapper.createArrayNode();

        String internalSerdePackage = "com.arangodb.internal.serde";
        Collection<URL> serdeUrls = ClasspathHelper.forPackage(internalSerdePackage);
        Reflections r = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false))
                .setUrls(serdeUrls)
                .filterInputsBy(new FilterBuilder().includePackage(internalSerdePackage)));
        Stream<String> serializers = r.getSubTypesOf(ValueSerializer.class).stream()
                .filter(it -> !it.isAnonymousClass())
                .map(Class::getName);
        Stream<String> deserializers = r.getSubTypesOf(ValueDeserializer.class).stream()
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
                .sorted()
                .distinct()
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
