package com.quorum.tessera.config.cli;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CliDelegate {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(CliDelegate.class);
    
    private Config config;

    public static CliDelegate instance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return config;
    }

    public CliResult execute(String... args) throws Exception {

        final List<String> argsList = Arrays.asList(args);

        final CliAdapter cliAdapter;

        if(argsList.contains("admin")) {
            cliAdapter = new AdminCliAdapter(new ClientFactory());
        } else {
            cliAdapter = ServiceLoaderUtil.load(CliAdapter.class)
                    .orElse(new DefaultCliAdapter());
        }

        LOGGER.info("Executing {} , with {}",cliAdapter.getClass(),String.join(",", args));
        
        final CliResult result = cliAdapter.execute(args);

        LOGGER.info("Executed {} , with {}",cliAdapter.getClass(),String.join(",", args));
        
        this.config = result.getConfig().orElse(null);
        return result;
    }

}
