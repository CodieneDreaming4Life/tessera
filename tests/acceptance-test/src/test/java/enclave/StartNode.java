package enclave;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.ProcessManager;
import cucumber.api.java8.En;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import util.Environment;

public class StartNode implements En {

    public StartNode() {
        Given("tessera node is running", () -> {

            URL configFileUrl = getClass().getResource("/enclave/node-config.json");
            URL otherConfigFileUrl = getClass().getResource("/enclave/other-node-config.json");
            
            String configfilePath = configFileUrl.getFile();
            String jarPath = Environment.INSTANCE.getJarPath();

            Map<String,URL> configs = new HashMap<>();
            configs.put("X", configFileUrl);
            configs.put("Y", otherConfigFileUrl);
            
            ProcessManager processManager = new ProcessManager(CommunicationType.REST,configs);
            
            processManager.startNodes();


        });
    }

}
