package com.example.queuectl.cli;

import com.example.queuectl.config.SpringContext;
import com.example.queuectl.service.WorkerManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "config",
    description = "Configuration commands",
    subcommands = { ConfigCommand.Set.class }
)
public class ConfigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Usage: queuectl config set <key> <value>");
    }

    @Command(name = "set", description = "Set configuration key-value pair")
    static class Set implements Runnable {

        @Parameters(index = "0", description = "Configuration key (e.g. backoff-base)")
        private String key;

        @Parameters(index = "1", description = "Configuration value")
        private String value;

        @Override
        public void run() {
            WorkerManager wm = SpringContext.getBean(WorkerManager.class);

            try {
                if ("backoff-base".equalsIgnoreCase(key)) {
                    int base = Integer.parseInt(value);
                    wm.setBackoffBase(base);
                    System.out.println("Set backoff-base = " + base);
                } else {
                    System.err.println("Unknown config key: " + key);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for key '" + key + "': " + value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
