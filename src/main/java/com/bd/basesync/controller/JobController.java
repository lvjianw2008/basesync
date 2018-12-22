package com.bd.basesync.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bd.basesync.entity.JobAndTrigger;
import com.bd.basesync.job.BaseJob;
import com.bd.basesync.service.IJobAndTriggerService;
import com.github.pagehelper.PageInfo;

import javax.sql.DataSource;


@RestController
@RequestMapping(value="/job")
public class JobController
{
    @Autowired
    private IJobAndTriggerService iJobAndTriggerService;

    //加入Qulifier注解，通过名称注入bean
    @Autowired @Qualifier("Scheduler")
    private Scheduler scheduler;

    private static Logger log = LoggerFactory.getLogger(JobController.class);


    @PostMapping(value="/addjob")
    public void addjob(@RequestParam(value="jobName")String jobName,
                       @RequestParam(value="jobClassName")String jobClassName,
                       @RequestParam(value="jobGroupName")String jobGroupName,
                       @RequestParam(value="cronExpression")String cronExpression) throws Exception
    {
        addJob(jobName,jobClassName, jobGroupName, cronExpression);
    }

    public void addJob(String jobName,String jobClassName, String jobGroupName, String cronExpression)throws Exception{

        // 启动调度器
        scheduler.start();

        //构建job信息
        JobDetail jobDetail = JobBuilder.newJob(getClass(jobClassName).getClass()).withIdentity(jobName, jobGroupName).build();

        //表达式调度构建器(即任务执行的时间)
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

        //按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName, jobGroupName)
                .withSchedule(scheduleBuilder).build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            System.out.println("创建定时任务失败"+e);
            throw new Exception("创建定时任务失败");
        }
    }


    @PostMapping(value="/pausejob")
    public void pausejob(@RequestParam(value="jobName")String jobName, @RequestParam(value="jobGroupName")String jobGroupName) throws Exception
    {
        jobPause(jobName, jobGroupName);
    }

    public void jobPause(String jobClassName, String jobGroupName) throws Exception
    {
        scheduler.pauseJob(JobKey.jobKey(jobClassName, jobGroupName));
    }


    @PostMapping(value="/resumejob")
    public void resumejob(@RequestParam(value="jobName")String jobName, @RequestParam(value="jobGroupName")String jobGroupName) throws Exception
    {
        jobresume(jobName, jobGroupName);
    }

    public void jobresume(String jobClassName, String jobGroupName) throws Exception
    {
        scheduler.resumeJob(JobKey.jobKey(jobClassName, jobGroupName));
    }


    @PostMapping(value="/reschedulejob")
    public void rescheduleJob(@RequestParam(value="jobName")String jobName,
                              @RequestParam(value="jobGroupName")String jobGroupName,
                              @RequestParam(value="cronExpression")String cronExpression) throws Exception
    {
        jobreschedule(jobName, jobGroupName, cronExpression);
    }

    public void jobreschedule(String jobName, String jobGroupName, String cronExpression) throws Exception
    {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            System.out.println("更新定时任务失败"+e);
            throw new Exception("更新定时任务失败");
        }
    }


    @PostMapping(value="/deletejob")
    public void deletejob(@RequestParam(value="jobName")String jobName, @RequestParam(value="jobGroupName")String jobGroupName) throws Exception
    {
        jobdelete(jobName, jobGroupName);
    }

    public void jobdelete(String jobName, String jobGroupName) throws Exception
    {
        scheduler.pauseTrigger(TriggerKey.triggerKey(jobName, jobGroupName));
        scheduler.unscheduleJob(TriggerKey.triggerKey(jobName, jobGroupName));
        scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
    }


    @GetMapping(value="/queryjob")
    public Map<String, Object> queryjob(@RequestParam(value="pageNum")Integer pageNum, @RequestParam(value="pageSize")Integer pageSize)
    {
        PageInfo<JobAndTrigger> jobAndTrigger = iJobAndTriggerService.getJobAndTriggerDetails(pageNum, pageSize);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("JobAndTrigger", jobAndTrigger);
        map.put("number", jobAndTrigger.getTotal());
        return map;
    }

    public static BaseJob getClass(String classname) throws Exception
    {
        Class<?> class1 = Class.forName(classname);
        return (BaseJob)class1.newInstance();
    }

    /**
     * 立即执行任务
     * LV
     * 2018年12月20日 14:50:00
     * @param jobName
     * @param jobGroupName
     */
    @PostMapping(value="/execJob")
    public void execJob(@RequestParam(value="jobName")String jobName, @RequestParam(value="jobGroupName")String jobGroupName) {
        execJob(jobName, jobGroupName,new JobDataMap());
    }


    public void execJob(String jobName, String jobGroupName,JobDataMap dataMap) {
        log.debug("execJob:" + jobName);
        try {
            scheduler.triggerJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 查询和清空缓存中的任务
     * LV
     * 2018年12月20日 14:50:00
     */
    @PostMapping(value="/getAllJobs")
    public void getAllJobs(){
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();
                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    System.out.println("[jobName] : " + jobName + " [groupName] : "
                            + jobGroup + " - " + nextFireTime);
                    jobdelete(jobName,groupName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}