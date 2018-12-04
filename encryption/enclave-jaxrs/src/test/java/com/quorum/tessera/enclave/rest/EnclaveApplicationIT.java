package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import java.util.Arrays;
import static java.util.Collections.singletonList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.bouncycastle.util.encoders.Hex;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnclaveApplicationIT extends JerseyTest {

    private Enclave enclave;

    @Override
    public void setUp() throws Exception {
        enclave = mock(Enclave.class);
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Mockito.verifyNoMoreInteractions(enclave);

    }

    @Override
    protected Application configure() {

        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig config = ResourceConfig.forApplicationClass(EnclaveApplication.class);
        config.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "FINE");
        config.packages("com.quorum.tessera.enclave.rest");
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(enclave).to(Enclave.class);
            }
        });
        //   config.register(JsonProcessingFeature.class);
        return config;
    }

    @Test
    public void ping() throws Exception {

        Response response = target("ping").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotEmpty();

    }

    @Test
    public void defaultKey() throws Exception {

        PublicKey publicKey = PublicKey.from("defaultKey".getBytes());

        when(enclave.defaultPublicKey()).thenReturn(publicKey);

        Response response = target("default").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("defaultKey");
        verify(enclave).defaultPublicKey();

    }

    @Test
    public void publicKeys() throws Exception {

        Set<PublicKey> keys = Stream.of("publicKey", "publicKey2")
                .map(String::getBytes)
                .map(PublicKey::from)
                .collect(Collectors.toSet());

        when(enclave.getPublicKeys())
                .thenReturn(keys);

        Response response = target("public").request().get();
        assertThat(response.getStatus()).isEqualTo(200);

        JsonArray result = response.readEntity(JsonArray.class);

        assertThat(result).hasSize(2);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void encryptPayload() {

        EncodedPayloadWithRecipients pay = createSample();

        when(enclave.encryptPayload(any(byte[].class), any(PublicKey.class), anyList()
        )).thenReturn(pay);

        EnclavePayload enclavePayload = new EnclavePayload();
        enclavePayload.setData("SOMEDATA".getBytes());
        enclavePayload.setSenderKey("SENDER_KEY".getBytes());
        enclavePayload.setRecipientPublicKeys(Arrays.asList("RecipientPublicKey".getBytes()));
        Response response = target("encrypt")
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .post(Entity.entity(enclavePayload, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus()).isEqualTo(200);
        
        byte[] result = response.readEntity(byte[].class);
        assertThat(result).isNotNull().isNotEmpty();
        
        
        verify(enclave).encryptPayload(any(byte[].class), any(PublicKey.class), anyList());
        
    }

    static EncodedPayloadWithRecipients createSample() {
        
        final String hexOutput = "00000000000000200542de47c272516862bae08c53f1cb034439a739184fe707208dd92817b2dc1a0000000000000179d2e6ee7f25feacc8b91a0366c326ff2569020a56067545495b51446a174a0c68c13f895ff0aede655926ed0817ba5a05f9f117f8a82f486999de0a6dd07281da290c034871c8a6ba7ce77f3c645f7f1fb89b1af4f76c36027c1637097b36f0331ce79a9ce959f156169cc192fee0ff0c8c66d55c0269b2b76f85c58ae02fc12948b823bc2d4d6ee88f96e1d60d85362d53dac7746bac16e2cf542711ecb586fa49c346cbbfea0d172b9b17101fffedf8a289e4819b2b1fe410b2aa2f2a15737faf2cdff4b6b36f00794643514a5a74f2b5529289e9544a3de1beb9963c7f8fe649ce90d35225bccf28b7cb55b952207519aff3e2d08aae7dc101d28d982002ff84a8ecb36c7b294e6ca8415442d84f8a3f93abcc089fcf57e5c14bd3330774bc1059350e873526f07ad192ed4866af0d0de49927e624f1c3a5c09d76ded38921395c775fef13322e895885cfbc974af1664aed1d4b8edecafa6f7a0237633ae17b32ac80474f13d85c074be18fc4f879695b81456acff3a5de00000000000000188e802f3106b991b49cf07182036b37012bfad5988083db1f0000000000000001000000000000003091d7e03ba7bbcde5404aa7c19f360cf6986f9c9e04224349c7d20f64ebd6f2d5484081d471f65269af7a3dce1c6cc8a40000000000000018922d2cb41117b400b57046616cbab42064d2bd6ba76240ab0000000000000001000000000000002044e019056b5269cc5742b39edc5180a890f226315e3d1e5c7b84d2233989d017";
        final byte[] encoded = Hex.decode(hexOutput);

        final byte[] sender = new byte[]{5, 66, -34, 71, -62, 114, 81, 104, 98, -70, -32, -116, 83, -15, -53, 3, 68, 57, -89, 57, 24, 79, -25, 7, 32, -115, -39, 40, 23, -78, -36, 26};
        final byte[] cipherText = new byte[]{-46, -26, -18, 127, 37, -2, -84, -56, -71, 26, 3, 102, -61, 38, -1, 37, 105, 2, 10, 86, 6, 117, 69, 73, 91, 81, 68, 106, 23, 74, 12, 104, -63, 63, -119, 95, -16, -82, -34, 101, 89, 38, -19, 8, 23, -70, 90, 5, -7, -15, 23, -8, -88, 47, 72, 105, -103, -34, 10, 109, -48, 114, -127, -38, 41, 12, 3, 72, 113, -56, -90, -70, 124, -25, 127, 60, 100, 95, 127, 31, -72, -101, 26, -12, -9, 108, 54, 2, 124, 22, 55, 9, 123, 54, -16, 51, 28, -25, -102, -100, -23, 89, -15, 86, 22, -100, -63, -110, -2, -32, -1, 12, -116, 102, -43, 92, 2, 105, -78, -73, 111, -123, -59, -118, -32, 47, -63, 41, 72, -72, 35, -68, 45, 77, 110, -24, -113, -106, -31, -42, 13, -123, 54, 45, 83, -38, -57, 116, 107, -84, 22, -30, -49, 84, 39, 17, -20, -75, -122, -6, 73, -61, 70, -53, -65, -22, 13, 23, 43, -101, 23, 16, 31, -1, -19, -8, -94, -119, -28, -127, -101, 43, 31, -28, 16, -78, -86, 47, 42, 21, 115, 127, -81, 44, -33, -12, -74, -77, 111, 0, 121, 70, 67, 81, 74, 90, 116, -14, -75, 82, -110, -119, -23, 84, 74, 61, -31, -66, -71, -106, 60, 127, -113, -26, 73, -50, -112, -45, 82, 37, -68, -49, 40, -73, -53, 85, -71, 82, 32, 117, 25, -81, -13, -30, -48, -118, -82, 125, -63, 1, -46, -115, -104, 32, 2, -1, -124, -88, -20, -77, 108, 123, 41, 78, 108, -88, 65, 84, 66, -40, 79, -118, 63, -109, -85, -52, 8, -97, -49, 87, -27, -63, 75, -45, 51, 7, 116, -68, 16, 89, 53, 14, -121, 53, 38, -16, 122, -47, -110, -19, 72, 102, -81, 13, 13, -28, -103, 39, -26, 36, -15, -61, -91, -64, -99, 118, -34, -45, -119, 33, 57, 92, 119, 95, -17, 19, 50, 46, -119, 88, -123, -49, -68, -105, 74, -15, 102, 74, -19, 29, 75, -114, -34, -54, -6, 111, 122, 2, 55, 99, 58, -31, 123, 50, -84, -128, 71, 79, 19, -40, 92, 7, 75, -31, -113, -60, -8, 121, 105, 91, -127, 69, 106, -49, -13, -91, -34};
        final byte[] nonce = new byte[]{-114, -128, 47, 49, 6, -71, -111, -76, -100, -16, 113, -126, 3, 107, 55, 1, 43, -6, -43, -104, -128, -125, -37, 31};
        final byte[] recipientBox = new byte[]{-111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111, -100, -98, 4, 34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105, -81, 122, 61, -50, 28, 108, -56, -92};
        final byte[] recipientNonce = new byte[]{-110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67, 107, -89, 98, 64, -85};
        final byte[] recipientKey = new byte[]{68, -32, 25, 5, 107, 82, 105, -52, 87, 66, -77, -98, -36, 81, -128, -88, -112, -14, 38, 49, 94, 61, 30, 92, 123, -124, -46, 35, 57, -119, -48, 23};

       return new EncodedPayloadWithRecipients(
                new EncodedPayload(
                        PublicKey.from(sender),
                        cipherText,
                        new Nonce(nonce),
                        singletonList(recipientBox),
                        new Nonce(recipientNonce)
                ),
                singletonList(PublicKey.from(recipientKey))
        );
    }
    
}
