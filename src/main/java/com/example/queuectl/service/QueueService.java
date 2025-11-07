package com.example.queuectl.service;

import com.example.queuectl.model.JobEntity;
import com.example.queuectl.repo.JobRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class QueueService {
    private final JobRepository jobRepo;

    public QueueService(JobRepository jobRepo) { this.jobRepo = jobRepo; }

    public JobEntity enqueue(JobEntity job) {
        Instant now = Instant.now();
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        job.setState("pending");
        job.setAttempts(0);
        if (job.getMaxRetries() == 0) job.setMaxRetries(3);
        return jobRepo.save(job);
    }

    public List<JobEntity> listByState(String state) { return jobRepo.findByState(state); }

    @Transactional
    public Optional<JobEntity> claimNextPending() {
        List<JobEntity> list = jobRepo.fetchPendingForProcessing(PageRequest.of(0, 1));
        if (list.isEmpty()) return Optional.empty();
        JobEntity j = list.get(0);
        j.setState("processing");
        j.setUpdatedAt(Instant.now());
        jobRepo.save(j);
        return Optional.of(j);
    }

    public void markCompleted(JobEntity job) {
        job.setState("completed");
        job.setUpdatedAt(Instant.now());
        jobRepo.save(job);
    }

    public void markFailedAndScheduleRetry(JobEntity job, int backoffBase, WorkerManager workerManager) {
        job.setAttempts(job.getAttempts() + 1);
        job.setUpdatedAt(Instant.now());
        if (job.getAttempts() > job.getMaxRetries()) {
            job.setState("dead");
            jobRepo.save(job);
        } else {
            job.setState("failed");
            jobRepo.save(job);
            long delay = (long) Math.pow(backoffBase, job.getAttempts());
            workerManager.scheduleReenqueue(job.getId(), delay);
        }
    }

    public void reenqueue(Long id) {
        jobRepo.findById(id).ifPresent(job -> {
            job.setState("pending");
            job.setUpdatedAt(Instant.now());
            jobRepo.save(job);
        });
    }
}
