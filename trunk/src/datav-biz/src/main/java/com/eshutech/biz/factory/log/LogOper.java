package com.eshutech.biz.factory.log;

import com.eshutech.biz.entity.TblLogOperation;
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
@TaskThread(model = "LogOper", initialDelay = 60, delay = 10)
public class LogOper extends BaseTaskThread {
    private static final Logger                      logger     = LoggerFactory.getLogger(LogOper.class);
    private static final BlockingQueue<TblLogOperation> matchQueue = new LinkedBlockingQueue<TblLogOperation>();

    public static void BI(String code, String msg, String session, String linkId, String ip, String agent, String content, Object... objects) {
        int isRun = LocalConfig.I().GI("LogOper", 0);
        if (isRun == 0) {
            if (objects != null && objects.length > 0) {
                content = MessageFormatter.arrayFormat(content, objects).getMessage();
            }
            logger.info(content);
            return;
        }
        try {
            TblLogOperation log = new TblLogOperation();
            log.setLoggerType(code);
            log.setLoggerDesc(msg);
            log.setLoggerSeesion(session);
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
            logger.debug("--------[LogOper] RUN:{}--------", size);
            int maxCount = LocalConfig.I().GI("LogOper_Max", 1000);
            List<TblLogOperation> logOperations = Lists.newArrayList();
            if (size > maxCount) {
                matchQueue.drainTo(logOperations, maxCount);
            } else {
                matchQueue.drainTo(logOperations, size);
            }
            if (logOperations != null && logOperations.size() > 0) {
                //TODO:insert log
            }
        } catch (Exception ex) {
            logger.error("--------[LogOper] Exception:{}--------", ExceptionUtils.getStackTrace(ex));
        }
    }
}
