package com.bd.basesync.service.impl;

import com.bd.basesync.dao.JobAndTriggerMapper;
import com.bd.basesync.dao.JobLogMapper;
import com.bd.basesync.entity.JobAndTrigger;
import com.bd.basesync.entity.JobLog;
import com.bd.basesync.service.IJobAndTriggerService;
import com.bd.basesync.service.IJobLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class JobLogImpl implements IJobLogService {

    @Autowired
    private JobLogMapper jobLogMapper;

    public PageInfo<JobLog> queryJobLogByJobName(int pageNum, int pageSize,String jobName) {
        PageHelper.startPage(pageNum, pageSize);
        List<JobLog> list = jobLogMapper.queryJobLogByJobName(jobName);
        PageInfo<JobLog> page = new PageInfo<JobLog>(list);
        return page;
    }

    @Override
    public void insertJobLog(JobLog jobLog) {
        jobLogMapper.insertJobLog(jobLog);
    }
}