package com.github.nexus.enclave.model;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageHashTest {

    @Test
    public void messageHashMakesCopyOfInput() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHash hash = new MessageHash(testMessage);

        assertThat(hash).isNotEqualTo(testMessage);
        assertThat(Arrays.equals(testMessage, hash.getHashBytes())).isTrue();

    }

    @Test
    public void differentInstancesOfSameBytesIsEqual() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHash hash1 = new MessageHash(testMessage);
        final MessageHash hash2 = new MessageHash(testMessage);

        assertThat(hash1).isEqualTo(hash2);

    }

    @Test
    public void differentObjectTypesAreNotEqual() {

        final byte[] testMessage = "test_message".getBytes();

        final MessageHash hash1 = new MessageHash(testMessage);

        assertThat(hash1).isNotEqualTo("test_message");

    }

    @Test
    public void sameObjectIsEqual() {
        final MessageHash hash = new MessageHash("I LOVE SPARROWS".getBytes());
        assertThat(hash).isEqualTo(hash).hasSameHashCodeAs(hash);
    }

    @Test
    public void toStringOutputsCorrectString() {

        final MessageHash hash = new MessageHash(new byte[]{5, 6, 7});

        final String toString = hash.toString();

        assertThat(toString).isEqualTo("[5, 6, 7]");

    }
    

}