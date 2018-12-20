package com.bd.basesync.job;

import java.sql.ResultSet;
import java.util.Date;

import com.bd.basesync.util.db.BaseDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HelloJob implements BaseJob {

    private static Logger _log = LoggerFactory.getLogger(HelloJob.class);

    public HelloJob() {

    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        _log.error("Hello Job执行时间: " + new Date());
//        try {
//            BaseDataSet set = new BaseDataSet();
//            _log.info("数据源创建完成 " + new Date());
//            ResultSet resultSet = set.executeQuery("select * from member LIMIT 0,10");
//            while (resultSet.next()) {
//                _log.info(resultSet.getString("1"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}