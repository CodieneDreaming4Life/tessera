package com.quorum.tessera.test;

import com.quorum.tessera.config.CommunicationType;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = "enclave",
        features = "classpath:features/enclave.feature"
)
public class CucumberRemoteEnclaveIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberRemoteEnclaveIT.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private static ProcessManager processManager;

    @BeforeClass
    public static void onSetup() throws Exception {

        URL configFileUrl = CucumberRemoteEnclaveIT.class.getResource("/enclave/node-config.json");
        URL otherConfigFileUrl = CucumberRemoteEnclaveIT.class.getResource("/enclave/other-node-config.json");

        processManager = ProcessManager.Builder.create()
                .withCommunicationType(CommunicationType.REST)
                .withConfigFile("X", configFileUrl)
                .withConfigFile("Y", otherConfigFileUrl).build();

        LOGGER.info("Starting");

        processManager.start("Y");
        
//        executorService.submit(() -> {
//            try {
//                processManager.start("Y");
//            } catch (Exception ex) {
//                throw new RuntimeException(ex);
//            }
//        });

//        executorService.submit(() -> {
//            try {
//                processManager.start("X");
//            } catch (Exception ex) {
//                throw new RuntimeException(ex);
//            }
//        });

        LOGGER.info("Started");

    }

    @AfterClass
    public static void onTearDown() throws Exception {
        LOGGER.info("Stopping");
        processManager.stopNodes();
        executorService.shutdown();
        LOGGER.info("Stopped");

    }
}
