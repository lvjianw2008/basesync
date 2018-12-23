package com.bd.basesync.controller;

import com.bd.basesync.entity.JobLog;
import com.bd.basesync.service.IJobLogService;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping(value="/")
public class IndexController
{
    @RequestMapping(value="/")
    public String Index()
    {
        return "forward:/JobManager.html";
    }

}