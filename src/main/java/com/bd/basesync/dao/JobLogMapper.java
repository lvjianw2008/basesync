package com.bd.basesync.dao;

import com.bd.basesync.entity.JobAndTrigger;
import com.bd.basesync.entity.JobLog;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface JobLogMapper {
    public List<JobLog> queryJobLogByJobName(String jobName);
    public void insertJobLog(JobLog jobLog);
}