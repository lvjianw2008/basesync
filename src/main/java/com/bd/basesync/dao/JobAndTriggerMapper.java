package com.bd.basesync.dao;

import java.util.List;

import com.bd.basesync.entity.JobAndTrigger;

public interface JobAndTriggerMapper {
    public List<JobAndTrigger> getJobAndTriggerDetails();
}