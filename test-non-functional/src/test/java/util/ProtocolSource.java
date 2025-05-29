package util;

import com.arangodb.Protocol;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@EnumSource(
        value = Protocol.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = "VST"
)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolSource {
}
