package com.bd.basesync;

import com.bd.basesync.entity.JobLog;
import com.bd.basesync.service.IJobLogService;
import com.bd.basesync.util.IDGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BasesyncApplicationTests {
	@Qualifier("dataSource")
	@Autowired
	private DataSource dataSource;

	@Autowired
	private IJobLogService iJobLogService;

	@Test
	public void contextLoads() {
		System.out.println("dataSource:"+dataSource);
	}


	@Test
	public void insertJobLog() {
		JobLog jobLog = new JobLog();
		jobLog.setFD_ID(IDGenerator.generateID());
		jobLog.setJOB_CLASS("TEST");
		jobLog.setJOB_COST_TIME(100);
		jobLog.setJOB_EXEC_TIME(new Date());
		jobLog.setJOB_UPDATE_COUNT(100);
		jobLog.setJOB_INSERT_COUNT(200);
		jobLog.setJOB_NAME("测试任务");
		jobLog.setJOB_STATUS("SUCCESS");
		jobLog.setJOB_LOG_DETAIL("任务细节");
		iJobLogService.insertJobLog(jobLog);
	}
}

