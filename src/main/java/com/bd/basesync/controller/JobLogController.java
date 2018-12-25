package com.bd.basesync.controller;

import com.bd.basesync.entity.JobAndTrigger;
import com.bd.basesync.entity.JobLog;
import com.bd.basesync.job.BaseJob;
import com.bd.basesync.service.IJobAndTriggerService;
import com.bd.basesync.service.IJobLogService;
import com.github.pagehelper.PageInfo;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value="/jobLog")
public class JobLogController
{
    @Autowired
    private IJobLogService iJobLogService;

    private static Logger log = LoggerFactory.getLogger(JobLogController.class);
    @PostMapping(value="/addJobLog")
    public void addjob(@RequestParam(value="jobName")String jobName,
                       @RequestParam(value="jobClassName")String jobClassName,
                       @RequestParam(value="jobGroupName")String jobGroupName,
                       @RequestParam(value="cronExpression")String cronExpression) throws Exception{

//        iJobLogService.insertJobLog();
    }

    @GetMapping(value="/queryJobLogByJobName")
    public Map<String, Object> queryjob(@RequestParam(value="pageNum")Integer pageNum, @RequestParam(value="pageSize")Integer pageSize,
                                        @RequestParam(value="jobName")String jobName)
    {
        PageInfo<JobLog> jobLogPage = iJobLogService.queryJobLogByJobName(pageNum, pageSize,jobName);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("JobLog", jobLogPage);
        map.put("number", jobLogPage.getTotal());
        return map;
    }

}