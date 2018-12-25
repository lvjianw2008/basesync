package com.bd.basesync.entity;


import java.text.SimpleDateFormat;
import java.util.Date;

public class JobLog {
    //日志ID
    private String FD_ID;
    //任务名称
    private String JOB_NAME;
    //任务状态
    private String JOB_STATUS;
    //任务执行时间
    private Date JOB_EXEC_TIME;
    //任务执行耗费秒数
    private Integer JOB_COST_TIME;
    //任务插入数据条数
    private Integer JOB_INSERT_COUNT;
    //任务更新数据条数
    private Integer JOB_UPDATE_COUNT;
    //任务执行路径
    private String JOB_CLASS;
    //任务详细日志
    private String JOB_LOG_DETAIL;

    public String getJOB_NAME() {
        return JOB_NAME;
    }

    public void setJOB_NAME(String JOB_NAME) {
        this.JOB_NAME = JOB_NAME;
    }

    public String getJOB_STATUS() {
        return JOB_STATUS;
    }

    public void setJOB_STATUS(String JOB_STATUS) {
        this.JOB_STATUS = JOB_STATUS;
    }

    public Date getJOB_EXEC_TIME() {
        return JOB_EXEC_TIME;
    }

    public void setJOB_EXEC_TIME(Date JOB_EXEC_TIME) {
        this.JOB_EXEC_TIME = JOB_EXEC_TIME;
    }

    public Integer getJOB_COST_TIME() {
        return JOB_COST_TIME;
    }

    public void setJOB_COST_TIME(Integer JOB_COST_TIME) {
        this.JOB_COST_TIME = JOB_COST_TIME;
    }

    public Integer getJOB_INSERT_COUNT() {
        return JOB_INSERT_COUNT;
    }

    public void setJOB_INSERT_COUNT(Integer JOB_INSERT_COUNT) {
        this.JOB_INSERT_COUNT = JOB_INSERT_COUNT;
    }

    public Integer getJOB_UPDATE_COUNT() {
        return JOB_UPDATE_COUNT;
    }

    public void setJOB_UPDATE_COUNT(Integer JOB_UPDATE_COUNT) {
        this.JOB_UPDATE_COUNT = JOB_UPDATE_COUNT;
    }

    public String getJOB_LOG_DETAIL() {
        return JOB_LOG_DETAIL;
    }

    public void setJOB_LOG_DETAIL(String JOB_LOG_DETAIL) {
        this.JOB_LOG_DETAIL = JOB_LOG_DETAIL;
    }

    public String getJOB_CLASS() {
        return JOB_CLASS;
    }

    public void setJOB_CLASS(String JOB_CLASS) {
        this.JOB_CLASS = JOB_CLASS;
    }

    public String getFD_ID() {
        return FD_ID;
    }

    public void setFD_ID(String FD_ID) {
        this.FD_ID = FD_ID;
    }
}