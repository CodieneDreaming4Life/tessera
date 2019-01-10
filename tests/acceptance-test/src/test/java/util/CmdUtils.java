package util;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.jaxb.UnmarshallerBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdUtils.class);

    public static ExecutionResult executeArgs(String... args) throws InterruptedException, IOException {
        return executeArgs(Arrays.asList(args));
    }

    public static ExecutionResult executeArgs(List<String> args) throws InterruptedException, IOException {

        LOGGER.info("Starting {}", String.join(" ", args));

        Config config = searchArgsForConfigFile(args)
                .orElseThrow(() -> new RuntimeException("Unable to find config from args " + String.join(",", args)));

        final URI uri = config.getServerConfigs()
                .stream()
                .filter(s -> s.getApp() == AppType.ENCLAVE)
                .filter(s -> s.isEnabled())
                .map(s -> s.getServerSocket().getServerUri())
                .findAny()
                .get();

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        ExecutionResult executionResult = new ExecutionResult();

        final StreamConsumer stdOutConsumer = new StreamConsumer(process.getInputStream(), false);
        final StreamConsumer errOutConsumer = new StreamConsumer(process.getErrorStream(), true);

        HttpCheck httpCheck = new HttpCheck(uri.toURL());

        Collection<? extends Callable<Void>> streamConsumers = Arrays.asList(
                stdOutConsumer,
                errOutConsumer,
                httpCheck
        );

        ExecutorService executorService = Executors.newCachedThreadPool();

        try{
            // Future<Boolean> outcome = executorService.submit(new HttpCheck(uri.toURL()));
            //  executorService.invokeAll(streamConsumers);

            executorService.submit(stdOutConsumer);
            executorService.submit(errOutConsumer);
            Future<Void> result = executorService.submit(httpCheck);

            stdOutConsumer.getOutput().forEach(executionResult::addOutputLine);
            errOutConsumer.getOutput().forEach(executionResult::addErrorLine);

            executorService.submit(() -> {
                try{
                    result.get();
                } catch (InterruptedException ex) {
                    LOGGER.warn(null, ex);
                } catch (ExecutionException ex) {
                    LOGGER.error(null, ex.getCause());
                }
            });

            executionResult.setExitCode(0);
            return executionResult;
        } finally {
            executorService.shutdown();
        }

    }

    static Optional<Config> searchArgsForConfigFile(List<String> args) {

        Iterator<String> it = args.iterator();
        while (it.hasNext()) {
            String token = it.next();
            if (token.contains("configfile")) {
                String pathToken = it.next();
                Path path = Paths.get(pathToken);
                try{
                    Config config = (Config) UnmarshallerBuilder.create()
                            .withXmlMediaType()
                            .withoutBeanValidation()
                            .build().unmarshal(path.toFile());
                    return Optional.of(config);
                } catch (JAXBException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }

        return Optional.empty();
    }

}
