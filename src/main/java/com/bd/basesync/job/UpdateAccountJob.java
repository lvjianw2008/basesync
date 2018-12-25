package com.bd.basesync.job;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.bd.basesync.entity.JobLog;
import com.bd.basesync.properties.NcConfig;
import com.bd.basesync.service.IJobLogService;
import com.bd.basesync.util.IDGenerator;
import com.bd.basesync.util.StringUtil;
import com.bd.basesync.util.db.BaseDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sun.swing.StringUIClientPropertyKey;

import javax.sql.DataSource;

/**
 * 定时同步 - 全量费用科目档案
 * XZ_UAP_KJKM - > KM_ADVANCE_ACCOUNT
 * LV 2018-12-22 13:52
 */
public class UpdateAccountJob implements BaseJob {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IJobLogService iJobLogService;

    @Autowired
    private NcConfig ncConfig;

    private static Logger _log = LoggerFactory.getLogger(UpdateAccountJob.class);
    public UpdateAccountJob() {
    }

    private static int addRowNum;
    private static int updateRowNum;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        addRowNum = 0; // 新增计数
        updateRowNum = 0; // 更新计数

        _log.error("全量更新费用科目Job开始: " + new Date());
        Long start = System.currentTimeMillis();
        JobLog jobLog = new JobLog();
        jobLog.setFD_ID(IDGenerator.generateID());
        jobLog.setJOB_CLASS(this.getClass().getName());
        jobLog.setJOB_EXEC_TIME(new Date());

        jobLog.setJOB_NAME(context.getTrigger().getJobKey().getName());
        BaseDataSet ncSet = null;
        try {
            ncSet = new BaseDataSet("nc");

            _log.info("数据源nc创建完成 " + new Date());

            String NcName = ncConfig.getNcName();
            int numOfdata = getNumOfData(ncSet,NcName);
            _log.info("本次更新共计 " + numOfdata+"条");
            if(numOfdata != 0){
                int projectNum=numOfdata;
                int forNum=projectNum/500;
                String inSql = ncConfig.getInsql();

                for(int k=0;k<forNum+1;k++){
                    BaseDataSet ncSet2 = null;
                    try {
                        ncSet2 = new BaseDataSet("nc");
                        String getSql=" SELECT  project.PK_CORP,project.PK_ACCSUBJ,project.UNITCODE,project.UNITNAME,project.SUBJCODE,project.SUBJNAME,project.TS,project.FC FROM" +
                                " ( SELECT ROW_NUMBER () OVER (ORDER BY PK_ACCSUBJ) AS num,PK_CORP,PK_ACCSUBJ,UNITCODE,UNITNAME,SUBJCODE,SUBJNAME,TS,FC	FROM "+NcName+".XZ_UAP_KJKM  WHERE \"LENGTH\"(UNITCODE)>3 and UNITNAME in "+inSql+") project" +
                                "  where project.num>'"+(k*500)+"' and project.num<='"+((k+1)*500)+"'";

                        ncSet2.beginTran();
                        ResultSet resultSet = ncSet2.executeQuery(getSql);
                        if (null != resultSet) {
                            _log.info("同步  第  " +(k*500) +"------"+((k+1)*500)+"  条数据");
                            synAccount(resultSet,jobLog);
                        }
                        ncSet2.commit();
                        ncSet2.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        ncSet2.rollback();
                    }finally {
                        if(ncSet2!=null){
                            ncSet2.close();
                        }
                    }
                }
            }
//            ResultSet ncResultSet = ncSet.executeQuery("select * from qrtz_triggers LIMIT 0,10");
//            while (ncResultSet.next()) {
//                _log.info("nc:"+ncResultSet.getString(1));
//            }
            jobLog.setJOB_STATUS("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            jobLog.setJOB_STATUS("ERROR");
        }finally {
            if(ncSet!=null){
                ncSet.close();
            }
            jobLog.setJOB_INSERT_COUNT(addRowNum);
            jobLog.setJOB_UPDATE_COUNT(updateRowNum);
            Long end = System.currentTimeMillis();
            Long costTime =((end-start) % (1000 * 60)) / 1000;
            jobLog.setJOB_COST_TIME(costTime.intValue());
            jobLog.setJOB_LOG_DETAIL("");
            iJobLogService.insertJobLog(jobLog);
        }
    }

    private int getNumOfData(BaseDataSet ncSet,String NcName){
        String num = "";
        String getCountSql = "select count(*) num from "+NcName+".XZ_UAP_KJKM WHERE \"LENGTH\"(UNITCODE)>3 and unitname like '%百大%'";
        try {
            ResultSet ncResultSet = ncSet.executeQuery(getCountSql);
            while(ncResultSet.next()){
                num = ncResultSet.getString("num");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(num);
    }

    private void synAccount(ResultSet resultSet,JobLog jobLog) {

        try {
            if (null != resultSet) {
                while (resultSet.next()) {
                    String fdId = null;
                    String accsubjPk = resultSet.getString("PK_ACCSUBJ"); // 会计科目主键

                    //通过NC数据库的科目主键来维护确定这个数据是否已经存在
                    String getCountSql = "select fd_id from km_advance_account where accsubj_pk = ?";
                    String existFdId = "";
                    try{
                        List<String> list = jdbcTemplate.queryForList(getCountSql,new Object[]{accsubjPk},String.class);
                        if(list!=null && list.size()>0){
                            existFdId = list.get(0);
                        }
                    }catch (DataAccessException e){
                        existFdId = "";
                    }

//                    ResultSet existSet = dataSource.getConnection().createStatement().executeQuery(getCountSql);
//                    String  existFdId ="";
//                    while(existSet.next()){
//                        existFdId = existSet.getString("fd_id");
//                    }
                    if (StringUtil.isNull(existFdId)) {
                        ++addRowNum;
//                            insertData(insertStatement, resultSet,addRowNum);
                        insertDataNew(resultSet);
                    } else {
                        ++updateRowNum;
//                            updateData(updateStatement, resultSet,updateRowNum, existFdId);
                        updateDataNew(resultSet,existFdId);
                    }


                }
            }
//            dataSource.getConnection().commit();
//            connection.close();
//            dataSet.close();

            System.out.println("新增：" + addRowNum + "条，更新：" + updateRowNum + "条科目数据");
        } catch (Exception e) {
            e.printStackTrace();
            jobLog.setJOB_STATUS("ERROR");
        }
    }


    /**
     * 插入数据
     *
     * @param resultSet
     * @throws SQLException
     */
    public void insertDataNew(ResultSet resultSet) throws SQLException {
        String fdId = IDGenerator.generateID();
        jdbcTemplate.update(
                "insert into KM_ADVANCE_ACCOUNT("
                        + "fd_id,FD_NAME,FD_ACCOUNT_CODE,FD_COMPANY_NAME,FD_COMPANY_CODE,ACCSUBJ_PK,FC_FLAG)"
                        + " values(?,?,?,?,?,?,?)",
                new Object[]{fdId,resultSet.getString("SUBJNAME"),resultSet.getString("SUBJCODE"),resultSet.getString("UNITNAME"),
                        resultSet.getString("UNITCODE"),resultSet.getString("PK_ACCSUBJ"),resultSet.getString("FC")},
                new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR}
        );
    }

    /**
     * 更新数据
     *
     * @param resultSet
     * @throws SQLException
     */
    public void updateDataNew(ResultSet resultSet,String fdId) throws SQLException {
        jdbcTemplate.update(
                "update KM_ADVANCE_ACCOUNT set "
                        + "FD_NAME=?,FD_ACCOUNT_CODE=?,FD_COMPANY_NAME=?,FD_COMPANY_CODE=?,ACCSUBJ_PK=?,FC_FLAG=?"
                        + " where fd_id=?",
                new Object[]{resultSet.getString("SUBJNAME"),resultSet.getString("SUBJCODE"),resultSet.getString("UNITNAME"),
                        resultSet.getString("UNITCODE"),resultSet.getString("PK_ACCSUBJ"),resultSet.getString("FC"),fdId},
                new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR}
        );
    }

    /**
     * 插入数据
     *
     * @param statement
     * @param resultSet
     * @param rowNum
     * @throws SQLException
     */
    public void insertData(PreparedStatement statement, ResultSet resultSet,int rowNum) throws SQLException {
        String fdId = IDGenerator.generateID();
        statement.setString(1, fdId);
        statement.setString(2, resultSet.getString("SUBJNAME"));
        statement.setString(3, resultSet.getString("SUBJCODE"));
        statement.setString(4, resultSet.getString("UNITNAME"));
        statement.setString(5, resultSet.getString("UNITCODE"));

        statement.setString(6, resultSet.getString("PK_ACCSUBJ"));
        statement.setString(7, resultSet.getString("FC"));
        statement.addBatch();
    }

    /**
     * 更新数据
     *
     * @param statement
     * @param resultSet
     * @param rowNum
     * @throws SQLException
     */
    public void updateData(PreparedStatement statement, ResultSet resultSet,int rowNum, String fdId) throws SQLException {
        statement.setString(1, resultSet.getString("SUBJNAME"));
        statement.setString(2, resultSet.getString("SUBJCODE"));
        statement.setString(3, resultSet.getString("UNITNAME"));
        statement.setString(4, resultSet.getString("UNITCODE"));
        statement.setString(5, resultSet.getString("PK_ACCSUBJ"));
        statement.setString(6, resultSet.getString("FC"));
        statement.setString(7, fdId);
        statement.addBatch();
    }
}