package com.bd.basesync.job;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import com.bd.basesync.util.db.BaseDataSet;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Component("taskJob")
public class NewJob implements BaseJob {
    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    private static Logger _log = LoggerFactory.getLogger(NewJob.class);
    public NewJob() {

    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        _log.error("New Job执行时间: " + new Date());
        BaseDataSet badaSet = null;
        BaseDataSet ncSet = null;
        System.out.println(dataSource);
//        List list = jdbcTemplate.queryForList("select * from qrtz_triggers");
//        _log.info("本地数据源查询条数： " + list.size());
        try {
            badaSet = new BaseDataSet("baida");
            ncSet = new BaseDataSet("nc");
            _log.info("数据源baida创建完成 " + new Date());
            _log.info("数据源nc创建完成 " + new Date());
            ResultSet bdResultSet = badaSet.executeQuery("select * from member LIMIT 0,10");
            while (bdResultSet.next()) {
                _log.info("baida:"+bdResultSet.getString(1));
            }

            ResultSet ncResultSet = ncSet.executeQuery("select * from qrtz_triggers LIMIT 0,10");
            while (ncResultSet.next()) {
                _log.info("nc:"+ncResultSet.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(badaSet!=null){
                badaSet.close();
            }
            if(ncSet!=null){
                ncSet.close();
            }
        }
    }
}