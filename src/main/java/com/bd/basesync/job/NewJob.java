package com.bd.basesync.job;

import java.sql.ResultSet;
import java.util.Date;

import com.bd.basesync.util.db.BaseDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NewJob implements BaseJob {
    private static Logger _log = LoggerFactory.getLogger(NewJob.class);
    public NewJob() {

    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        _log.error("New Job执行时间: " + new Date());
        BaseDataSet set = null;
        try {
            set = new BaseDataSet("baida");
            _log.info("数据源创建完成 " + new Date());
            ResultSet resultSet = set.executeQuery("select * from member LIMIT 0,10");
            while (resultSet.next()) {
                _log.info(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(set!=null){
                set.close();
            }
        }
    }
}