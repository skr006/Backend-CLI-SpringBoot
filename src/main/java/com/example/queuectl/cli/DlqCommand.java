package com.example.queuectl.cli;

import java.util.List;
import java.util.concurrent.Callable;

import com.example.queuectl.config.SpringContext;
import com.example.queuectl.model.JobEntity;
import com.example.queuectl.repo.JobRepository;
import com.example.queuectl.service.QueueService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


@Command(
    name = "dlq",
    description = "Dead Letter Queue commands",
    subcommands = {DlqCommand.ListCommand.class, DlqCommand.RetryCommand.class}
)
public class DlqCommand {


    @Command(name = "list", description = "List all dead jobs")
    static class ListCommand implements Runnable {
        @Override
        public void run() {
            JobRepository repo = SpringContext.getBean(JobRepository.class);
            List<JobEntity> deadJobs = repo.findByState("dead");

            if (deadJobs.isEmpty()) {
                System.out.println("No jobs in the Dead Letter Queue.");
                return;
            }

            System.out.println("🧾 Dead Letter Queue Jobs:");
            for (JobEntity job : deadJobs) {
                System.out.printf("  ID: %s | Attempts: %d | Command: %s%n",
                        job.getId(), job.getAttempts(), job.getCommand());
            }
        }
    }


    @Command(name = "retry", description = "Retry a dead job by ID")
    static class RetryCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Job ID to retry")
        private Long id;

        @Override
        public Integer call() {
            JobRepository repo = SpringContext.getBean(JobRepository.class);
            QueueService queueService = SpringContext.getBean(QueueService.class);
            JobEntity job = repo.findById(id).orElse(null);
            if (job == null) {
                System.out.println("Job not found: " + id);
                return 1;
            }

            job.setAttempts(0);
            job.setState("pending");
            repo.save(job);
            queueService.reenqueue(id);

            System.out.println("Job requeued successfully: " + id);
            return 0;
        }
    }
}
