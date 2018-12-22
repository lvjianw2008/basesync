package com.bd.basesync.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

public interface BaseJob extends Job{
    public void execute(JobExecutionContext context) throws JobExecutionException;
}