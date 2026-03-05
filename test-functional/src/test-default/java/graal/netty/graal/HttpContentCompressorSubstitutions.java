package graal.netty.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class HttpContentCompressorSubstitutions {

    @TargetClass(className = "io.netty.handler.codec.compression.Zstd")
    public static final class Target_io_netty_handler_codec_compression_Zstd {
        @Substitute
        public static boolean isAvailable() {
            return false;
        }
    }

    @TargetClass(className = "io.netty.handler.codec.compression.ZstdEncoder")
    public static final class Target_io_netty_handler_codec_compression_ZstdEncoder {

        @Substitute
        protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Substitute
        protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
            throw new UnsupportedOperationException();
        }

        @Substitute
        public void flush(final ChannelHandlerContext ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Substitute
    @TargetClass(className = "io.netty.handler.codec.compression.ZstdConstants")
    public static final class Target_io_netty_handler_codec_compression_ZstdConstants {

        // The constants make <clinit> calls to com.github.luben.zstd.Zstd so we cut links with that substitution.

        static final int DEFAULT_COMPRESSION_LEVEL = 0;

        static final int MIN_COMPRESSION_LEVEL = 0;

        static final int MAX_COMPRESSION_LEVEL = 0;

        static final int DEFAULT_MAX_ENCODE_SIZE = 0;

        static final int DEFAULT_BLOCK_SIZE = 0;
    }

}
