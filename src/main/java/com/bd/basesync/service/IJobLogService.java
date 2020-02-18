package com.bd.basesync.service;

import com.bd.basesync.entity.JobAndTrigger;
import com.bd.basesync.entity.JobLog;
import com.github.pagehelper.PageInfo;

public interface IJobLogService {
    public PageInfo<JobLog> queryJobLogByJobName(int pageNum, int pageSize, String jobName);
    public void insertJobLog(JobLog jobLog);
}