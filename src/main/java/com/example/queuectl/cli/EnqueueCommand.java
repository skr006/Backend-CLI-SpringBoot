package com.example.queuectl.cli;

import com.example.queuectl.model.JobEntity;
import com.example.queuectl.service.QueueService;
import com.example.queuectl.config.SpringContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "enqueue", description = "Enqueue a job JSON")
public class EnqueueCommand implements Callable<Integer> {
    @Parameters(index = "0", description = "Job JSON")
    private String jsonJob;

    @Override
    public Integer call() throws Exception {
        ObjectMapper om = new ObjectMapper();
        JobEntity job = om.readValue(jsonJob, JobEntity.class);
        QueueService qs = SpringContext.getBean(QueueService.class);
        qs.enqueue(job);
        System.out.println("Enqueued " + job.getId());
        return 0;
    }
}
