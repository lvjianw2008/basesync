package com.bd.basesync.service;

import com.bd.basesync.entity.JobAndTrigger;
import com.bd.basesync.entity.JobLog;
import com.github.pagehelper.PageInfo;

public interface IJobAndTriggerService {
    public PageInfo<JobAndTrigger> getJobAndTriggerDetails(int pageNum, int pageSize);
}