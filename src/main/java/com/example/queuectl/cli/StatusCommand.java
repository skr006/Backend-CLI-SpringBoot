package com.example.queuectl.cli;

import com.example.queuectl.config.SpringContext;
import com.example.queuectl.repo.JobRepository;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.stream.Collectors;

@Command(name = "status", description = "Show job counts by state")
public class StatusCommand implements Runnable {
    @Override
    public void run() {
        JobRepository repo = SpringContext.getBean(JobRepository.class);
        Map<String, Long> counts = repo.findAll().stream()
                .collect(Collectors.groupingBy(j -> j.getState(), Collectors.counting()));
        System.out.println("Job counts by state:");
        counts.forEach((k,v) -> System.out.printf("  %s: %d\n", k, v));
    }
}
