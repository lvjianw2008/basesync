package com.bd.basesync.dao;

import java.util.List;

import com.bd.basesync.entity.JobAndTrigger;
import org.springframework.stereotype.Component;

@Component
public interface JobAndTriggerMapper {
    public List<JobAndTrigger> getJobAndTriggerDetails();
}