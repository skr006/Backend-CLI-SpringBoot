package com.example.queuectl;

import com.example.queuectl.cli.MainCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

@SpringBootApplication
public class QueueCtlApplication implements ApplicationRunner {

    @Autowired
    private ApplicationContext ctx;

    public static void main(String[] args) {
        new SpringApplicationBuilder(QueueCtlApplication.class)
                .logStartupInfo(false)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MainCommand main = ctx.getBean(MainCommand.class);
        int exitCode = new CommandLine(main)
                .setExecutionStrategy(new CommandLine.RunLast())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args.getSourceArgs());

        System.exit(exitCode);
    }
}
