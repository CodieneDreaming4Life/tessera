package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.parsers.ConfigurationParser;
import com.quorum.tessera.config.cli.parsers.KeyGenerationParser;
import com.quorum.tessera.config.cli.parsers.KeyUpdateParser;
import com.quorum.tessera.config.cli.parsers.PidFileParser;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.PasswordReaderFactory;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.*;

public class DefaultCliAdapter implements CliAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCliAdapter.class);

    private final Validator validator = Validation.byDefaultProvider()
        .configure()
        .ignoreXmlConfiguration()
        .buildValidatorFactory()
        .getValidator();

    @Override
    public CliResult execute(String... args) throws Exception {

        Options options = this.buildBaseOptions();

        Map<String, Class> overrideOptions = OverrideUtil.buildConfigOptions();

        overrideOptions.forEach((optionName, optionType) -> {

            final boolean isCollection = optionType.isArray();

            Option.Builder optionBuilder = Option.builder()
                .longOpt(optionName)
                .desc(String.format("Override option for %s , type: %s", optionName, optionType.getSimpleName()));

            if (isCollection) {
                optionBuilder.hasArgs().argName(optionType.getSimpleName().toUpperCase() + "...");
            } else {
                optionBuilder.hasArg().argName(optionType.getSimpleName().toUpperCase());
            }

            options.addOption(optionBuilder.build());

        });

        final List<String> argsList = Arrays.asList(args);
        if (argsList.contains("help") || argsList.isEmpty()) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(200);
            formatter.printHelp("tessera -configfile <PATH> [-keygen <PATH>] [-pidfile <PATH>]", options);
            return new CliResult(0, true, null);
        }

        try {

            final CommandLine line = new DefaultParser().parse(options, args);

            final Config config = parseConfig(line);

            if (Objects.nonNull(config)) {

                overrideOptions.forEach((optionName, value) -> {
                    if (line.hasOption(optionName)) {
                        String[] values = line.getOptionValues(optionName);
                        LOGGER.debug("Setting : {} with value(s) {}", optionName, values);
                        OverrideUtil.setValue(config, optionName, values);
                        LOGGER.debug("Set : {} with value(s) {}", optionName, values);
                    }
                });

                Set<ConstraintViolation<Config>> violations = validator.validate(config);
                if (!violations.isEmpty()) {
                    throw new ConstraintViolationException(violations);
                }
            }

            new PidFileParser().parse(line);

            boolean suppressStartup = line.hasOption("keygen") && Objects.isNull(config);

            return new CliResult(0, suppressStartup, config);

        } catch (ParseException exp) {
            throw new CliException(exp.getMessage());
        }

    }

    private Config parseConfig(CommandLine commandLine) throws IOException {

        if(commandLine.hasOption("updatepassword")) {
            new KeyUpdateParser(
                KeyEncryptorFactory.create(),
                PasswordReaderFactory.create()
            ).parse(commandLine);

            //return early so other options don't get processed
            return null;
        }

        final List<ConfigKeyPair> newKeys = new KeyGenerationParser().parse(commandLine);

        final Config config = new ConfigurationParser().withNewKeys(newKeys).parse(commandLine);

        if (!commandLine.hasOption("configfile") && !commandLine.hasOption("keygen") && !commandLine.hasOption("updatepassword")) {
            throw new CliException("One or more: -configfile or -keygen or -updatepassword options are required.");
        }

        return config;
    }

    private Options buildBaseOptions() {

        final Options options = new Options();

        options.addOption(
            Option.builder("configfile")
                .desc("Path to node configuration file")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("PATH")
                .build());

        options.addOption(
            Option.builder("keygen")
                .desc("Use this option to generate public/private keypair")
                .hasArg(false)
                .build());

        options.addOption(
            Option.builder("filename")
                .desc("Comma-separated list of paths to save generated key files. Can also be used with keyvault. Number of args equals number of key-pairs generated.")
                .hasArgs()
                .optionalArg(false)
                .argName("PATH")
                .build());

        options.addOption(
            Option.builder("keygenconfig")
                .desc("Path to private key config for generation of missing key files")
                .hasArg(true)
                .optionalArg(false)
                .argName("PATH")
                .build());

        options.addOption(
            Option.builder("output")
                .desc("Generate updated config file with generated keys")
                .hasArg(true)
                .numberOfArgs(1)
                .build());

        options.addOption(
            Option.builder("keygenvaulttype")
                .desc("Type of key vault the generated key is to be saved in")
                .hasArg()
                .optionalArg(false)
                .argName("KEYVAULTTYPE")
                .build()
        );

        options.addOption(
            Option.builder("keygenvaulturl")
                .desc("Base url for key vault")
                .hasArg()
                .optionalArg(false)
                .argName("STRING")
                .build()
        );

        options.addOption(
            Option.builder("keygenvaultapprole")
                  .desc("AppRole path for Hashicorp Vault authentication (defaults to 'approle')")
                  .hasArg()
                  .optionalArg(false)
                  .argName("STRING")
                  .build()
        );

        options.addOption(
            Option.builder("keygenvaultcert")
                .desc("TLS certificate for Hashicorp Vault authentication")
                .hasArg()
                .optionalArg(false)
                .argName("PATH")
                .build()
        );

        options.addOption(
            Option.builder("keygenvaultcertkey")
                  .desc("TLS key for Hashicorp Vault authentication")
                  .hasArg()
                  .optionalArg(false)
                  .argName("PATH")
                  .build()
        );

        options.addOption(
            Option.builder("keygenvaultservercert")
                  .desc("TLS certificate of Vault server for Hashicorp Vault authentication")
                  .hasArg()
                  .optionalArg(false)
                  .argName("PATH")
                  .build()
        );

        options.addOption(
            Option.builder("pidfile")
                .desc("Path to pid file")
                .hasArg(true)
                .optionalArg(false)
                .numberOfArgs(1)
                .argName("PATH")
                .build());

        options.addOption(
            Option.builder("updatepassword")
                .desc("Update the password for a locked key")
                .hasArg(false)
                .build()
        );

        return options;
    }

}
