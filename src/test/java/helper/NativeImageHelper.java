package helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reflections.Reflections;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

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

        List<String> packages = Arrays.asList("com.arangodb.entity", "com.arangodb.model");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode rootNode = mapper.createArrayNode();
        ObjectNode noArgConstructor = mapper.createObjectNode();
        noArgConstructor.put("name", "<init>");
        noArgConstructor.set("parameterTypes", mapper.createArrayNode());
        ArrayNode methods = mapper.createArrayNode();
        methods.add(noArgConstructor);

        packages.stream()
                .flatMap(p -> {
                    final ConfigurationBuilder config = new ConfigurationBuilder()
                            .setScanners(new MethodParameterScanner())
                            .setUrls(ClasspathHelper.forPackage(p))
                            .filterInputsBy(new FilterBuilder().includePackage(p));

                    return new Reflections(config).getConstructorsMatchParams().stream();
                })
                .filter((it -> Modifier.isPublic(it.getDeclaringClass().getModifiers())))
                .filter(it -> Modifier.isPublic(it.getModifiers()))
                .map(Constructor::getName)
                .map(className -> {
                    ObjectNode entry = mapper.createObjectNode();
                    entry.put("name", className);
                    entry.put("allDeclaredFields", true);
                    entry.set("methods", methods);
                    return entry;
                })
                .forEach(rootNode::add);

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        System.out.println(jsonString);
    }
}
