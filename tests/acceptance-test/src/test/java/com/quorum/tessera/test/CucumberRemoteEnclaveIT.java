
package com.quorum.tessera.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    glue = "enclave",
    features = "classpath:features/enclave.feature"
)
public class CucumberRemoteEnclaveIT {
    
}
