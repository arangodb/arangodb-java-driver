package graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;


@TargetClass(className = "io.netty.handler.codec.compression.Brotli")
final class Target_io_netty_handler_codec_compression_Brotli {
    @Substitute
    public static boolean isAvailable() {
        return false;
    }

    @Substitute
    public static void ensureAvailability() throws Throwable {
        throw new UnsupportedOperationException();
    }
}

public class BrotliSubstitutions {

}
