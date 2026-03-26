package utils;

import com.arangodb.Protocol;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EnumSource(Protocol.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolSource {
}
