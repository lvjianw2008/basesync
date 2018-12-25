package com.bd.basesync.job;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.bd.basesync.service.IJobLogService;
import com.bd.basesync.util.db.BaseDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class HelloJob implements BaseJob {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IJobLogService iJobLogService;

    private static Logger _log = LoggerFactory.getLogger(HelloJob.class);
    public HelloJob() {
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        _log.error("测试开始: " + new Date());
        List<String> list = null;
        String id = "";
        try{
            list = jdbcTemplate.queryForList("select t.fd_id from SYS_ORG_ELEMENT t where  t.fd_org_type = ?",new Object[]{4},String.class);
            if(list!=null && list.size()>0){
                id = list.get(0);
            }
        }catch (DataAccessException e){
            e.printStackTrace();
        }
        _log.error("ID: " + id);
    }
}