package com.itheima.utils;

import lombok.SneakyThrows;

import java.util.LinkedList;

/**
 * @author v-rr
 */
public class CrawlerFactory implements Runnable {

    int max = 6;
    LinkedList<Crawler> spare = new LinkedList<>();

    void newPool() {
        for (int i = 0; i < max; i++) {
            spare.add(new Crawler());
        }
    }

    public CrawlerFactory() {
        newPool();
    }

    public void setMax(int max) {
        this.max = max;
        newPool();
    }

    public synchronized void clear() {
        spare.clear();
    }

    synchronized Crawler build() {
        if (spare.size() > 0) {
            return spare.poll();
        } else {
            return null;
        }
    }


    void back(Crawler crawler) {
        crawler.clear();
        spare.add(crawler);
    }

    @SneakyThrows
    @Override
    public void run() {
        Crawler crawler = this.build();
        if (crawler == null) {
            System.out.println("Empty pool, wait for change. ");
        }
//            if( Crawler.getTaskType()==2 &&Crawler.getInfo().getSlowTaskMap().size()==0){
//                System.out.println("getSlowTaskMap is done. ");
//                Task.saveRecords();
//
//             throw new InterruptedException();
//            }
        else {
            try {
                crawler.run();
                back(crawler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
