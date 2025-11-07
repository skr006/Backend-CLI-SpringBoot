package com.example.queuectl.service;

import com.example.queuectl.config.SpringContext;
import com.example.queuectl.model.JobEntity;
import com.example.queuectl.repo.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
@Service
public class WorkerManager {

    @Autowired
    private JobRepository jobRepository;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ExecutorService executorService;

    private int backoffBase = 2; // default backoff

    /** Start a fixed number of worker threads */
    public void startWorkers(int count) {
        executorService = Executors.newFixedThreadPool(count);
        System.out.println("Starting " + count + " worker(s)...");

        for (int i = 0; i < count; i++) {
            executorService.submit(() -> {
                try {
                    runWorkerLoop();
                } catch (Exception e) {
                    System.err.println("Worker loop error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    /** Stop all running workers */
    public void stopWorkers() {
        if (executorService != null) {
            executorService.shutdownNow();
            System.out.println("All workers stopped.");
        }
    }

    /** Set backoff base for retry delay calculation */
    public void setBackoffBase(int backoffBase) {
        this.backoffBase = backoffBase;
    }
    
    public void scheduleReenqueue(Long jobId, long delaySeconds) {
        scheduler.schedule(() -> {
            System.out.println("Re-enqueueing job " + jobId);
            // Get QueueService bean from Spring context
            QueueService queueService = SpringContext.getBean(QueueService.class);
            queueService.reenqueue(jobId);
        }, delaySeconds, TimeUnit.SECONDS);
    }
    
    /** Continuous worker loop polling pending jobs */
    private void runWorkerLoop() throws InterruptedException {
        while (true) {
            List<JobEntity> pendingJobs = jobRepository.findByState("pending");

            if (!pendingJobs.isEmpty()) {
                JobEntity job = pendingJobs.get(0); // pick first pending job
                System.out.println("Picked job " + job.getId());

                job.setState("processing");
                job.setUpdatedAt(Instant.now());
                jobRepository.save(job);

                try {
                    String output = runCommand(job.getCommand());
                    System.out.println("Job " + job.getId() + " output:\n" + output);
                    job.setState("completed");
                } catch (IOException e) {
                    job.setAttempts(job.getAttempts() + 1);
                    if (job.getAttempts() > job.getMaxRetries()) {
                        job.setState("dead");
                        System.err.println("Job " + job.getId() + " moved to DLQ (max retries reached).");
                    } else {
                        job.setState("failed");
                        System.err.println("Job " + job.getId() + " failed: " + e.getMessage());
                    }
                }

                job.setUpdatedAt(Instant.now());
                jobRepository.save(job);
            } else {
                Thread.sleep(2000); // wait 2s before next poll
            }
        }
    }

    /** Run a system command and capture only its output */
    private String runCommand(String command) throws IOException {
        ProcessBuilder pb;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            pb = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            pb = new ProcessBuilder("/bin/sh", "-c", command);
        }

        Process process = pb.start();

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            String errLine;
            while ((errLine = errReader.readLine()) != null) {
                output.append(errLine).append("\n"); // include stderr if needed
            }

            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Command failed with exit code " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return output.toString().trim();
    }
}
