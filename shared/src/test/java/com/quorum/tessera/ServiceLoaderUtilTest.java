package com.quorum.tessera;

import com.acme.Bogus;
import com.acme.VeryBogus;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ServiceLoaderUtilTest {

    @Test
    public void noServiceFound() {
        Optional<ServiceLoaderUtilTest> result = ServiceLoaderUtil.load(ServiceLoaderUtilTest.class);
        assertThat(result).isNotPresent();
    }

    @Test
    public void serviceFound() {
        Optional<Bogus> result = ServiceLoaderUtil.load(Bogus.class);
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(VeryBogus.class);

    }

    @Test
    public void loadPrefered() {

        Optional<Bogus> result = ServiceLoaderUtil.load(Bogus.class, VeryBogus.class);
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(VeryBogus.class);
    }

    @Test
    public void loadPreferedPreferenceNotFound() {

        Optional<Bogus> result = ServiceLoaderUtil.load(Bogus.class, EvenMoreBogus.class);
        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(VeryBogus.class);
    }

    static class EvenMoreBogus implements Bogus {
    }

}
