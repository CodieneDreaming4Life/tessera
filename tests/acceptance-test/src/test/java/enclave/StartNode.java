package enclave;

import cucumber.api.java8.En;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import util.Environment;
import util.ExecutionResult;

public class StartNode implements En {

    public StartNode() {
        Given("tessera node is running", () -> {

            String configfilePath = getClass().getResource("/enclave/node-config.json").getFile();
            String jarPath = Environment.INSTANCE.getJarPath();

            URL logbackConfigFile = getClass().getResource("/logback-node.xml");

            List<String> args = Arrays.asList(
                    "java",
                    "-Ddebug=true",
                    "-Dspring.profiles.active=disable-unixsocket,disable-sync-poller",
                    "-Dlogback.configurationFile=" + logbackConfigFile.getFile(),
                    "-Dnode.number=Y",
                    "-jar",
                    jarPath,
                    "-configfile",
                    configfilePath
            );

            ExecutionResult result = util.CmdUtils.executeArgs(args);

            assertThat(result.getExitCode())
                    .describedAs("Start nodes command: %s ", String.join(" ", args))
                    .isEqualTo(0);

        });
    }

}
