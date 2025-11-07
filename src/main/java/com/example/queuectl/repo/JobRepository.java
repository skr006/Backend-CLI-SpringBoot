package com.example.queuectl.repo;

import com.example.queuectl.model.JobEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {

    List<JobEntity> findByState(String state);

    @Query("SELECT j FROM JobEntity j WHERE j.state = 'pending' ORDER BY j.createdAt ASC")
    List<JobEntity> fetchPendingForProcessing(Pageable pageable);
}
