package util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdUtils.class);

    public static ExecutionResult executeArgs(List<String> args) throws InterruptedException, IOException {

        LOGGER.info("Starting {}", String.join(" ", args));

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        ExecutionResult executionResult = new ExecutionResult();

        Collection<StreamConsumer> streamConsumers = Arrays.asList(
                new StreamConsumer(process.getErrorStream(), true),
                new StreamConsumer(process.getInputStream(), false)
        );

        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            executorService.invokeAll(streamConsumers);

            //FIXME: 
            streamConsumers.stream()
                    .map(StreamConsumer::getOutput)
                    .flatMap(List::stream)
                    .forEach(executionResult::addOutputLine);

            streamConsumers.stream()
                    .map(StreamConsumer::getErrors)
                    .flatMap(List::stream)
                    .forEach(executionResult::addErrorLine);

            executionResult.setExitCode(process.waitFor());
        } finally {
            executorService.shutdown();
        }

        return executionResult;
    }

}
