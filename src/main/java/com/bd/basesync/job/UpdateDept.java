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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * 定时同步 - 全量更新部门档案
 * XZ_UAP_BMDA - > KM_ADVANCE_DEPT
 * LV 2018-12-22 16:49
 */
public class UpdateDept implements BaseJob {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NcConfig ncConfig;

    @Autowired
    private IJobLogService iJobLogService;

    private static int addRowNum;
    private static int updateRowNum;

    private static Logger _log = LoggerFactory.getLogger(UpdateDept.class);
    public UpdateDept() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        addRowNum = 0; // 新增计数
        updateRowNum = 0; // 更新计数
        _log.error("全量更新部门Job开始: " + new Date());
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
            String inSql = ncConfig.getInsql();
            int numOfdata = getNumOfData(ncSet,NcName,inSql);
            _log.info("本次更新共计 " + numOfdata+"条");
            if(numOfdata != 0){
                int projectNum=numOfdata;
                int forNum=projectNum/500;

                for(int k=0;k<forNum+1;k++){
                    BaseDataSet ncSet2 = null;
                    try {
                        ncSet2 = new BaseDataSet("nc");
                        String getSql=" SELECT  distinct  project.PK_DEPTDOC,project.UNITCODE,project.UNITNAME,project.DEPTCODE,project.DEPTNAME,project.TS,project.FC   FROM" +
                                " ( SELECT ROW_NUMBER () OVER (ORDER BY PK_DEPTDOC) AS num,PK_DEPTDOC,UNITCODE,UNITNAME,DEPTCODE,DEPTNAME,TS,FC FROM "+NcName+".XZ_UAP_BMDA ) project" +
                                "  where project.num>'"+(k*500)+"' and project.num<='"+((k+1)*500)+"'";

                        ncSet2.beginTran();
                        ResultSet resultSet = ncSet2.executeQuery(getSql);
                        if (null != resultSet) {
                            _log.info("同步  第  " +(k*500) +"------"+((k+1)*500)+"  条数据");
                            synDept(resultSet,jobLog);
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
        String getCountSql = "Select distinct   count(*) num  from "+NcName+".XZ_UAP_BMDA";
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

    private void synDept(ResultSet resultSet,JobLog jobLog) {

        try {
            if (null != resultSet) {
                while (resultSet.next()) {
                    String fdId = null;
                    String deptPk = resultSet.getString("PK_DEPTDOC"); // 部门主键

                    //通过NC数据库的科目主键来维护确定这个数据是否已经存在
                    String getCountSql = "select fd_id from km_advance_dept where dept_pk = ?";
                    String  existFdId = "";
                    try{
                        List<String> list = jdbcTemplate.queryForList(getCountSql,new Object[]{deptPk},String.class);
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
                    + "条部门数据");
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
                ("insert into KM_ADVANCE_DEPT("
                        + "fd_id,FD_DEPT_NAME,FD_DEPT_CODE,FD_COMPANY_NAME,FD_COMPANY_CODE,DEPT_PK,FC_FLAG)"
                        + " values(?,?,?,?,?,?,?)"),
                new Object[]{fdId,resultSet.getString("DEPTNAME"),resultSet.getString("DEPTCODE"),resultSet.getString("UNITNAME"),
                        resultSet.getString("UNITCODE"),resultSet.getString("PK_DEPTDOC"),resultSet.getString("FC")},
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
                "update KM_ADVANCE_DEPT set "
                        + "FD_DEPT_NAME=?,FD_DEPT_CODE=?,FD_COMPANY_NAME=?,FD_COMPANY_CODE=?,DEPT_PK=?,FC_FLAG=?"
                        + " where fd_id=?",
                new Object[]{resultSet.getString("DEPTNAME"),resultSet.getString("DEPTCODE"),resultSet.getString("UNITNAME"),
                        resultSet.getString("UNITCODE"),resultSet.getString("PK_DEPTDOC"),resultSet.getString("FC"),fdId},
                new int[]{java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR,java.sql.Types.VARCHAR}
        );
    }
}