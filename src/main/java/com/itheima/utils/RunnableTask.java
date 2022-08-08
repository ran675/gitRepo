package com.itheima.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author tarzan
 * @version 1.0
 * @date 2020/8/5$ 10:12$
 * @since JDK1.8
 */
@Component
@Slf4j
public class RunnableTask implements Runnable {

    private Integer i = 0;

    @Override
    public void run() {
        i++;
        log.info("===================round " + i + "======================");
        log.info("=============round" + i + "start================");
        //reptileService.crawling();
        log.info("=============round" + i + "end==================");
    }

}