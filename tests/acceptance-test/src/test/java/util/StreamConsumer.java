package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StreamConsumer implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamConsumer.class);

    private final InputStream inputStream;

    private boolean isError = false;

    private final List<String> lines = new ArrayList<>();

    StreamConsumer(InputStream inputStream, boolean isError) {
        this.inputStream = inputStream;
        this.isError = isError;
    }

    @Override
    public Void call() throws Exception {
        try (BufferedReader reader = Stream.of(inputStream)
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .findAny()
                .get()) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (isError) {
                    LOGGER.error(line);
                } else {
                    LOGGER.info(line);
                }
            }
            return null;
        }
    }

    public List<String> getErrors() {
        return isError ? lines : Collections.EMPTY_LIST;
    }

    public List<String> getOutput() {
        return isError ? Collections.EMPTY_LIST : lines;
    }
}
