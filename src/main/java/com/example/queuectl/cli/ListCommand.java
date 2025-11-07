package com.example.queuectl.cli;

import com.example.queuectl.config.SpringContext;
import com.example.queuectl.model.JobEntity;
import com.example.queuectl.repo.JobRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "list", description = "List jobs by state")
public class ListCommand implements Runnable {
    @Option(names = {"--state"}, description = "Job state to filter", required = true)
    String state;

    @Override
    public void run() {
        JobRepository repo = SpringContext.getBean(JobRepository.class);
        List<JobEntity> jobs = repo.findByState(state);
        System.out.println("Jobs (state=" + state + "):");
        for (JobEntity j : jobs) System.out.printf("  %s  attempts=%d\n", j.getId(), j.getAttempts());
    }
}
