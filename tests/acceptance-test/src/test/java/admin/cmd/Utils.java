package admin.cmd;

import util.ExecutionResult;
import com.quorum.tessera.test.Party;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CmdUtils;

public class Utils {

    private static String jarPath = System.getProperty("application.jar", "../../tessera-app/target/tessera-app-0.8-SNAPSHOT-app.jar");

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static ExecutionResult executeArgs(String... args) throws InterruptedException, IOException {
        return executeArgs(Arrays.asList(args));
    }
    
    public static ExecutionResult executeArgs(List<String> args) throws InterruptedException, IOException {
        return CmdUtils.executeArgs(args);
    }

    public static ExecutionResult start(Party party) throws IOException, InterruptedException {

        List<String> args = Arrays.asList(
                "java",
                "-Dspring.profiles.active=disable-unixsocket,disable-sync-poller",
                "-Dnode.number=" + party.getAlias(),
                "-jar",
                jarPath,
                "-configfile",
                party.getConfigFilePath().toString()
        );

        return executeArgs(args);
       

    }

    public static int addPeer(Party party, String url) throws IOException, InterruptedException {

        List<String> args = Arrays.asList(
                "java",
                "-jar",
                jarPath,
                "-configfile",
                party.getConfigFilePath().toAbsolutePath().toString(),
                "admin",
                "-addpeer",
                url
        );

        return executeArgs(args).getExitCode();

    }

  

}
