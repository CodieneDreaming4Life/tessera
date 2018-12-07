package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Set;
import com.quorum.tessera.server.TesseraServerFactory;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        ServiceLocator serviceLocator = ServiceLocator.create();

        Set<Object> services = serviceLocator.getServices("tessera-enclave-jaxrs-spring.xml");

        TesseraServerFactory restServerFactory = TesseraServerFactory.create(CommunicationType.REST);

        EnclaveResource enclaveResource = services.stream()
                .filter(EnclaveResource.class::isInstance)
                .map(EnclaveResource.class::cast)
                .findAny().get();

        final EnclaveApplication application = new EnclaveApplication(enclaveResource);

        final Config config = services.stream()
                .filter(Config.class::isInstance)
                .map(Config.class::cast)
                .findFirst().get();

        final ServerConfig serverConfig = config.getServerConfigs().stream()
                .findFirst().get();

        TesseraServer server = restServerFactory.createServer(serverConfig, Collections.singleton(application));

        CountDownLatch countDown = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try{
                server.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                countDown.countDown();
            }
        }));

        server.start();

        countDown.await();

    }

}
