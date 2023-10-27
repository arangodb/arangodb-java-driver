package resilience.utils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class MemoryAppender extends ListAppender<ILoggingEvent> {

    public MemoryAppender() {
        setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        start();
        Logger logger = (Logger) LoggerFactory.getLogger("root");
        logger.addAppender(this);
    }

    public void reset() {
        this.list.clear();
    }

    public List<ILoggingEvent> getLoggedEvents() {
        return Collections.unmodifiableList(this.list);
    }
}