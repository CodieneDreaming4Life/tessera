package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Set;
import com.quorum.tessera.server.TesseraServerFactory;
import java.util.Collections;

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

        Config config = services.stream()
                .filter(Config.class::isInstance)
                .map(Config.class::cast)
                .findAny().get();

        final ServerConfig serverConfig = config.getServerConfigs().iterator().next();

        TesseraServer server = restServerFactory.createServer(serverConfig, Collections.singleton(application));

        server.start();

        System.in.read();

        server.stop();

    }

}
