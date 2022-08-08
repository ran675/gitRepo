package com.itheima.utils;

import com.itheima.entity.Info;
import com.itheima.entity.Result;
import com.itheima.pojo.Record;
import com.itheima.pojo.Repo;
import com.itheima.service.DynamicTaskService;
import com.itheima.service.PlanService;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Controller
@RequestMapping("/task")
public class Task {

    public static String time = "0/2 * * * * ?";
    public static String sTime = "0/10 * * * * ?";

    public static void change() {
        time = sTime;
    }

    @Autowired
    private PlanService planService;

    @Autowired
    private DynamicTaskService taskService;

    @Autowired
    private RedisTemplate<Serializable, Serializable> redisTemplate;

    public static Task task;

    @PostConstruct
    public void init() {
        task = this;
        task.taskService = taskService;
        task.planService = planService;
        task.redisTemplate = redisTemplate;
    }


    @PostMapping("/start")
    @ResponseBody
    public Result start(Integer id, Integer[] repoIds) {
        List<Repo> execute = planService.execute(id);
        System.out.println(execute);
        taskService.startCron(execute, id);
        return new Result(true, "success start task");
    }

    @GetMapping("/change")
    @ResponseBody
    public Result changeT() {
        taskService.changeCron();
        return new Result(true, "success change task");
    }

    @GetMapping("/stop")
    @ResponseBody
    public Result stop() {
        try {
            HashMap<Integer, Record> recordHashMap = Crawler.getInfo().getRecordHashMap();

            ValueOperations<Serializable, Serializable> operations = redisTemplate.opsForValue();
            operations.set("recordHashMap" + Crawler.planId, recordHashMap, 60 * 60, TimeUnit.SECONDS);

            planService.saveRecords(recordHashMap);
            planService.updateExecuted(Crawler.planId, "Y");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "failed to stop task");
        }
        taskService.stopCron();
        return new Result(true, "success stop task");
    }

    static ScheduledExecutorService service;
    static ScheduledExecutorService slowTaskService;
    //    static ScheduledExecutorService npmTaskService;
    static CrawlerFactory factory;
    static int commonTime = 2000;
    static int searchTime = 10000;
    //    static int npmTime = 8000;
    static int delay = 500;

    static void toLine(BufferedReader reader, int startLine) throws IOException {
        int lines = 0;
        while (reader.ready() && lines < startLine - 1) {
            reader.readLine(); // 读到跳过startLine - 1行
            lines++;
        }
    }
//    @Scheduled(cron = "0/2 * * * * ?")
//    public void doTask() {
//        Thread thread = new Thread( factory );
//        thread.start();
////        System.out.println(thread.getName() + "===============begin" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
//    }
//
//    @Scheduled(cron = "0/8 * * * * ?")
//    public void doSlowTask() {
//        Thread thread = new Thread( factory );
//        thread.start();
//    }

    public static Info run(List<Repo> list, Integer planId) {
        Info info = new Info();
        info.setList(list);

        Integer startLine = 0;
        Integer endLine = 0;

        Repo repo = list.get(0);
        startLine = repo.getId();
        endLine = list.get(list.size() - 1).getId();

        String range = startLine + "_" + endLine;
        String toFilePath = "D:\\temp\\Json\\" + range;

        File file = new File(toFilePath);
        boolean mkdir = file.mkdir();
        File file1 = new File(toFilePath + "\\Serialized");
        File file2 = new File(toFilePath + "\\Entity");
        File file3 = new File(toFilePath + "\\Result");
//        File file4 = new File(toFilePath + "\\SlowTask");
        String toFile = toFilePath + "\\Result\\" + range + ".txt";
        String toFile1 = toFilePath + "\\Result\\" + range + "_total.txt";
        file1.mkdir();
        file2.mkdir();
        file3.mkdir();
//        file4.mkdir();
//        BufferedReader reader;
//        ---------------------------------
        PrintWriter out;
        try {
            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(toFile, true))));
            info.initWriter(toFile1);

            Crawler.setInfo(info);
            Crawler.setOut(out);
            Crawler.setToFileDirectory(toFilePath);
            Crawler.setPlanId(planId);

            config();

            Task.factory = new CrawlerFactory();
//            PooledCrawlerFactory.newPool();
//            PooledCrawlerFactory.setMax(4);

//            Task.task(commonTime);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

//    public void saveRecords(){
//        planService.updateExecuted(Crawler.planId, "Y");
//        System.out.println(Crawler.getInfo().getRecordHashMap().size());
//        planService.saveRecords(Crawler.getInfo().getRecordHashMap());
//        System.out.println(planService);
//        System.out.println("save all the results...");
//    }

    static void config() {
        //        ---------------------------------
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000) //连接超时时间
                .setConnectionRequestTimeout(500) //从线程池中获取线程超时时间
                .setSocketTimeout(8000) //设置数据超时时间
                .setStaleConnectionCheckEnabled(true) //提交请求前检查连接是否可用
                .setCookieSpec(CookieSpecs.STANDARD).build();
        //设置最大连接数
        cm.setMaxTotal(4000);

        //设置每个主机的最大连接数
        cm.setDefaultMaxPerRoute(40);

        Crawler.setCm(cm);
        Crawler.setConfig(config);

    }


    public static void searchTask(int slow) {
        if (!service.isShutdown()) {
            service.shutdown();
        }
//        Task.factory = new PooledCrawlerFactory(4);
        factory.setMax(6);
        slowTaskService = Executors.newScheduledThreadPool(10);
        System.out.println("slowTaskService = " + slowTaskService);
        slowTaskService.scheduleAtFixedRate(factory, delay, slow, TimeUnit.MILLISECONDS);
    }

    public static CrawlerFactory getFactory() {
        return factory;
    }

    public static void setFactory(CrawlerFactory factory) {
        Task.factory = factory;
    }

    public static void clearFac() {
        factory.clear();
    }

    public static void searchTask() {
        searchTask(searchTime);
    }

    public static void task(int fast) {
        service = Executors.newScheduledThreadPool(10);
        System.out.println("com.ran.service = " + service);
        service.scheduleAtFixedRate(factory, delay, fast, TimeUnit.MILLISECONDS);
    }

    public static void shutDown() throws Exception {
        if (slowTaskService != null) {
            if (!slowTaskService.isShutdown()) {
                slowTaskService.shutdownNow();
            }
//            throw new InterruptedException();
        }
    }
}
