package enclave;

import cucumber.api.java8.En;
import static org.assertj.core.api.Assertions.assertThat;
import util.ExecutionResult;
import util.Environment;
import util.CmdUtils;

public class StartEnclave implements En {

    public StartEnclave() {
        Given("enclave is running", () -> {
            
            String enclaveJarPath = Environment.INSTANCE.getEnclaveJarPath();
            
            String configFilePath = getClass().getResource("/enclave/enclave-config.xml").getFile();
            
            ExecutionResult result = CmdUtils.executeArgs("java","-jar",enclaveJarPath,"-configfile",configFilePath);
            
            assertThat(result).isNotNull();

        });
    }

}
