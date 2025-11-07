package com.example.queuectl.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "queuectl", mixinStandardHelpOptions = true, description = "queuectl CLI",
        subcommands = { EnqueueCommand.class, WorkerCommand.class, StatusCommand.class, ListCommand.class, DlqCommand.class, ConfigCommand.class })
public class MainCommand implements Runnable {
    @Override
    public void run() { System.out.println("queuectl - use --help for commands"); }
}
