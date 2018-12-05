package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Set;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:/tessera-enclave-jaxrs-spring.xml")
public class EnclaveRestIT {

    @Inject
    private Enclave enclave;

    private JerseyTest jersey;

    private EnclaveClient enclaveClient;

    @Before
    public void setUp() throws Exception {
        jersey = Util.create(enclave);
        jersey.setUp();

        enclaveClient = new EnclaveClient(jersey.client(), jersey.target().getUri());
    }

    @After
    public void tearDown() throws Exception {
        jersey.tearDown();

    }

    @Test
    public void defaultPublicKey() {
        PublicKey result = enclaveClient.defaultPublicKey();

        assertThat(result).isNotNull();
        assertThat(result.encodeToBase64())
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");

    }

    @Test
    public void forwardingKeys() {
        Set<PublicKey> result = enclaveClient.getForwardingKeys();

        assertThat(result).isEmpty();

    }

    @Test
    public void getPublicKeys() {
        Set<PublicKey> result = enclaveClient.getPublicKeys();

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().encodeToBase64())
                .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
    }

}
