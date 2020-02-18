package com.bd.basesync;

import com.bd.basesync.entity.JobLog;
import com.bd.basesync.service.IJobLogService;
import com.bd.basesync.util.IDGenerator;
import com.bd.basesync.util.StringUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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

	@Test
	public void test1(){
		String fdAllotRight = "{\"taskId\":\"169b95f54dad559f938ca9842518d46a\",\"processId\":\"169b95f543ff5dcb290623e48d4a721a\",\"activityType\":\"draftWorkitem\",\"operationType\":\"drafter_submit\",\"param\":{\"operationName\":\"提交文档\",\"notifyLevel\":\"3\",\"auditNote\":\"1\\n1\",\"auditNoteFdId\":\"169b95ffb3f7bb89e0b4ddd421f8289a\",\"identityId\":\"15aa1288960a18b91449f874792a05b1\",\"notifyOnFinish\":true,\"dayOfNotifyDrafter\":\"0\",\"hourOfNotifyDrafter\":\"0\",\"minuteOfNotifyDrafter\":\"0\"}}";
		JSONObject obj =  JSONObject.fromObject(fdAllotRight);
		JSONObject paramobj = (JSONObject) obj.get("param");
		if(paramobj!=null){
			String auditNote = (String) paramobj.get("auditNote");
			if(StringUtil.isNotNull(auditNote)){
				paramobj.put("auditNote","test");
			}
		}
		System.out.println(obj.toString());
	}

	@Test
	public void test2() {
		JSONObject json = new JSONObject();
		JSONArray leaderArray = new JSONArray();//直属领导审批
		JSONObject leaderObject = new JSONObject();
		leaderObject.put("Id","123" );
		leaderArray.add(leaderObject);
		json.put("fd_this_leader",  leaderArray);
		System.out.println(json.toString());
	}
}

