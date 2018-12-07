package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EnclaveFactory;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Collection;
import com.quorum.tessera.keypairconverter.KeyPairConverter;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.encryption.EnclaveImpl;
import com.quorum.tessera.encryption.KeyManagerImpl;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.nacl.NaclFacadeFactory;
import java.util.Optional;
import javax.ws.rs.client.Client;

public class DefaultEnclaveFactory implements EnclaveFactory<Config> {

    @Override
    public Enclave create(Config config) {

        Optional<ServerConfig> enclaveServerConfig = config.getServerConfigs().stream()
                    .filter(sc -> sc.getApp() == AppType.ENCLAVE)
                    .findAny();
        
        if (enclaveServerConfig.isPresent()) {

            final ClientFactory clientFactory = new ClientFactory();

            ServerConfig serverConfig = enclaveServerConfig.get();

            Client client = clientFactory.buildFrom(serverConfig);

            return new EnclaveClient(client, serverConfig.getServerUri());

        }

        KeyPairConverter keyPairConverter = new KeyPairConverter(config, new EnvironmentVariableProvider());
        Collection<KeyPair> keys = keyPairConverter.convert(config.getKeys().getKeyData());

        Collection<PublicKey> forwardKeys = com.quorum.tessera.encryption.KeyFactory.convert(config.getAlwaysSendTo());

        return new EnclaveImpl(NaclFacadeFactory.newFactory().create(), new KeyManagerImpl(keys, forwardKeys));
    }

}
