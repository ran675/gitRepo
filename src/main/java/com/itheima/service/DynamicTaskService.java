package com.itheima.service;

import com.itheima.mapper.RecordMapper;
import com.itheima.pojo.Repo;
import com.itheima.utils.Crawler;
import com.itheima.utils.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledFuture;


/**
 * @author tarzan
 * @version 1.0
 * @date 2020/8/5$ 10:07$
 * @since JDK1.8
 */
@Slf4j
@Service
public class DynamicTaskService {


    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;


    private ScheduledFuture<?> future;


    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    public void startCron(List<Repo> map, Integer planId) {
        String cron = Task.time;
//        Task.run(map, planId);
        if (cron == null) {
            log.error("Scheduled tasks FAIL to start...");
        } else {
            future = threadPoolTaskScheduler.schedule(Task.getFactory(), triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext));
//            future = threadPoolTaskScheduler.schedule(Task.getFactory(), triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext));
            log.info("Scheduled tasks start SUCCESS ...");
        }
    }


    public void changeCron() {
        Task.change();
        String cron = Task.time;
        if (Crawler.getInfo().getSlowTaskMap().size() == 0) {
            Task.task.stop();
        } else {
            if (future != null) {
                future.cancel(true);
            }
        }
        if (cron == null) {
            log.error("Scheduled tasks FAIL to start...");
        } else {
            future = threadPoolTaskScheduler.schedule(Task.getFactory(), triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext));
//            future = threadPoolTaskScheduler.schedule(Task.getFactory(), triggerContext -> new CronTrigger(cron).nextExecutionTime(triggerContext));
            log.info("Scheduled tasks change SUCCESS...");
        }
    }

    public void stopCron() {
        if (future != null) {
            future.cancel(true);
        }
        log.info("Scheduled tasks STOP ...");
    }

}
