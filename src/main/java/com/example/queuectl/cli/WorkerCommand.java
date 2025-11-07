package com.example.queuectl.cli;

import com.example.queuectl.config.SpringContext;
import com.example.queuectl.service.WorkerManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "worker", description = "Manage workers", subcommands = { WorkerCommand.Start.class, WorkerCommand.Stop.class })
public class WorkerCommand {

    @Command(name = "start", description = "Start workers")
    static class Start implements Callable<Integer> {
        @Option(names = {"--count"}, description = "Number of workers")
        int count = 1;

        @Override
        public Integer call() throws Exception {
            WorkerManager wm = SpringContext.getBean(WorkerManager.class);
            wm.startWorkers(count);
            System.out.println("Workers started (in-process). Press Ctrl+C to stop.");
            Thread.currentThread().join();
            return 0;
        }
    }

    @Command(name = "stop", description = "Stop workers")
    static class Stop implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            WorkerManager wm = SpringContext.getBean(WorkerManager.class);
            wm.stopWorkers();
            System.out.println("Stop signal sent to workers");
            return 0;
        }
    }
}
