package com.eshutech.biz.factory.log;


import com.eshutech.biz.entity.TblLogException;
import com.eshutech.biz.task.BaseTaskThread;
import com.eshutech.biz.util.annotation.TaskThread;
import com.eshutech.biz.util.helper.LocalConfig;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author:Lajiao
 * @Date:2014年10月21日
 * @Time:上午8:23:04
 * @Description:异常日志记录
 */
@TaskThread(model = "LogEx", initialDelay = 60, delay = 10)
public class LogEx extends BaseTaskThread {
    private static final Logger logger     = LoggerFactory.getLogger(LogEx.class);
    private static final BlockingQueue<TblLogException> matchQueue = new LinkedBlockingQueue<TblLogException>();

    public static void BI(String code, String model, String linkId, String content, Object[] objects) {
        int isRun = LocalConfig.I().GI("LogEx", 0);
        if (isRun == 0) {
            if (objects != null && objects.length > 0) {
                content = MessageFormatter.arrayFormat(content, objects).getMessage();
            }
            logger.info(content);
            return;
        }
        try {
            TblLogException log = new TblLogException();
            log.setLoggerType(code);
            log.setLoggerDesc(code);
            log.setLoggerSeesion(model);
            log.setLoggerLinkid(linkId);
            log.setLoggerIp(LocalConfig.I().Host());
            log.setLoggerAgent(LocalConfig.I().Port());
            if (objects != null && objects.length > 0) {
                content = MessageFormatter.arrayFormat(content, objects).getMessage();
            }
            log.setLoggerContent(content.length() > 8096 ? content.substring(0, 8096) : content);
            log.setCreateTime(new Date());
            matchQueue.put(log);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void BI(String code, String model, String linkId, String ip, String agent, String content, Object[] objects) {
        int isRun = LocalConfig.I().GI("LogEx", 0);
        if (isRun == 0) {
            if (objects != null && objects.length > 0) {
                content = MessageFormatter.arrayFormat(content, objects).getMessage();
            }
            logger.info(content);
            return;
        }
        try {
            TblLogException log = new TblLogException();
            log.setLoggerType(code);
            log.setLoggerDesc(code);
            log.setLoggerSeesion(model);
            log.setLoggerLinkid(linkId);
            log.setLoggerIp(ip);
            log.setLoggerAgent(agent);
            if (objects != null && objects.length > 0) {
                content = MessageFormatter.arrayFormat(content, objects).getMessage();
            }
            log.setLoggerContent(content.length() > 8096 ? content.substring(0, 8096) : content);
            log.setCreateTime(new Date());
            matchQueue.put(log);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void runFunction() {
        try {
            int size = matchQueue.size();
            logger.debug("--------[LogEx] RUN:{}--------", size);
            int maxCount = LocalConfig.I().GI("LogEx_Max", 1000);
            List<TblLogException> logExceptions = Lists.newArrayList();
            if (size > maxCount) {
                matchQueue.drainTo(logExceptions, maxCount);
            } else {
                matchQueue.drainTo(logExceptions, size);
            }
            if (logExceptions != null && logExceptions.size() > 0) {
                //TODO:insert log
            }
        } catch (Exception ex) {
            logger.error("--------[LogEx] Exception:{}--------", ExceptionUtils.getStackTrace(ex));
        }
    }
}
