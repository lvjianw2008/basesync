package com.bd.basesync.job;

import com.bd.basesync.entity.JobLog;
import com.bd.basesync.properties.NcConfig;
import com.bd.basesync.service.IJobLogService;
import com.bd.basesync.util.IDGenerator;
import com.bd.basesync.util.StringUtil;
import com.bd.basesync.util.db.BaseDataSet;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 定时同步 - 全量更新客商档案
 * XZ_UAP_KSDA - > KM_ADVANCE_BUSINESS
 * LV 2018-12-22 15:49
 */
public class UpdateBusinessJob implements BaseJob {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NcConfig ncConfig;

    @Autowired
    private IJobLogService iJobLogService;

    private static Logger _log = LoggerFactory.getLogger(UpdateBusinessJob.class);
    public UpdateBusinessJob() {
    }
    private static int addRowNum;
    private static int updateRowNum;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        addRowNum = 0; // 新增计数
        updateRowNum = 0; // 更新计数
        _log.error("全量更新客商Job开始: " + new Date());
        BaseDataSet ncSet = null;

        Long start = System.currentTimeMillis();
        JobLog jobLog = new JobLog();
        jobLog.setFD_ID(IDGenerator.generateID());
        jobLog.setJOB_CLASS(this.getClass().getName());
        jobLog.setJOB_EXEC_TIME(new Date());

        jobLog.setJOB_NAME(context.getTrigger().getJobKey().getName());

        try {
            ncSet = new BaseDataSet("nc");
            _log.info("数据源nc创建完成 " + new Date());

            String NcName = ncConfig.getNcName();
            String inSql = ncConfig.getInsql();
            int numOfdata = getNumOfData(ncSet,NcName,inSql);
            _log.info("本次更新共计 " + numOfdata+"条");
            if(numOfdata != 0){
                int projectNum=numOfdata;
                int forNum=projectNum/500;
                //先清空数据
                jdbcTemplate.execute("Truncate Table KM_ADVANCE_BUSINESS");

                for(int k=0;k<forNum+1;k++){
                    BaseDataSet ncSet2 = null;
                    try {
                        ncSet2 = new BaseDataSet("nc");
                        String getSql=" SELECT  distinct  project.PK,project.UNITCODE,project.UNITNAME,project.CUSTCODE,project.CUSTNAME,project.TS,project.ACCOUNT0,project.BANKDOCNAME,project.TS2,project.FC  FROM"
                                + " ( SELECT ROW_NUMBER () OVER (ORDER BY PK,UNITCODE,CUSTCODE DESC) AS num,PK,UNITCODE,UNITNAME,CUSTCODE,CUSTNAME,TS,ACCOUNT0,BANKDOCNAME,TS2,FC	FROM "
                                + NcName
                                + ".XZ_UAP_KSDA  WHERE \"LENGTH\"(UNITCODE)>3 and unitname in"+inSql+") project"
                                + "  where project.num>'"
                                + (k * 500)
                                + "' and project.num<='" + ((k + 1) * 500) + "'";

                        ncSet2.beginTran();
                        ResultSet resultSet = ncSet2.executeQuery(getSql);
                        if (null != resultSet) {
                            _log.info("同步  第  " +(k*500) +"------"+((k+1)*500)+"  条数据");
                            synBusiness(resultSet,jobLog);
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

    private int getNumOfData(BaseDataSet ncSet,String NcName,String inSql){
        String num = "";
        String getCountSql = "Select distinct   count(*) num  from "
                + NcName + ".XZ_UAP_KSDA WHERE \"LENGTH\"(UNITCODE)>3 and unitname in"+inSql;
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

    private void synBusiness(ResultSet resultSet,JobLog jobLog) {

        try {
            if (null != resultSet) {
                while (resultSet.next()) {
                    String fdId = null;
                    String businessPk = resultSet.getString("PK"); // 公司主键
                    String unitCode = resultSet.getString("UNITCODE"); // 公司编码
                    String fdBusinessCode = resultSet.getString("CUSTCODE"); // 客商编码

                    //通过NC数据库的科目主键来维护确定这个数据是否已经存在
                    String getCountSql = "select fd_id from km_advance_business where fd_business_code = ? and fd_company_code =? and business_pk = ?";
                    String  existFdId = "";
                    try{
                        List<String> list = jdbcTemplate.queryForList(getCountSql,new Object[]{fdBusinessCode,unitCode,businessPk},String.class);
                        if(list!=null && list.size()>0){
                            existFdId = list.get(0);
                        }
                    }catch (DataAccessException e){
                        existFdId = "";
                    }
                    if (StringUtil.isNull(existFdId)) {
                        ++addRowNum;
                        insertDataNew(resultSet);
                    } else {
                        ++updateRowNum;
                        updateDataNew(resultSet,existFdId);
                    }


                }
            }
            System.out.println("新增：" + addRowNum + "条，更新：" + updateRowNum
                    + "条客商数据");
        } catch (Exception e) {
            e.printStackTrace();
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
                "insert into KM_ADVANCE_BUSINESS("
                        + "fd_id,FD_BUSINESS_CODE,FD_BUSINESS_NAME,FD_BUSINESS_BANK,FD_BANK_CARD,FD_COMPANY_CODE,FD_COMPANY_NAME,FC_FLAG,BUSINESS_PK)"
                        + " values(?,?,?,?,?,?,?,?,?)",
                new Object[]{fdId,resultSet.getString("CUSTCODE"),resultSet.getString("CUSTNAME"),resultSet.getString("BANKDOCNAME"),
                        resultSet.getString("ACCOUNT0"),resultSet.getString("UNITCODE"),resultSet.getString("UNITNAME")
                        ,resultSet.getString("FC"),resultSet.getString("PK")},
                new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR}
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
                "update KM_ADVANCE_BUSINESS set "
                        + "FD_BUSINESS_CODE=?,FD_BUSINESS_NAME=?,FD_BUSINESS_BANK=?,FD_BANK_CARD=?,FD_COMPANY_CODE=?,FD_COMPANY_NAME=?,FC_FLAG=?,BUSINESS_PK=?"
                        + " where fd_id=?",
                new Object[]{resultSet.getString("CUSTCODE"),resultSet.getString("CUSTNAME"),resultSet.getString("BANKDOCNAME"),
                        resultSet.getString("ACCOUNT0"),resultSet.getString("UNITCODE"),resultSet.getString("UNITNAME")
                        ,resultSet.getString("FC"),resultSet.getString("PK"),fdId},
                new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR}
        );
    }
}