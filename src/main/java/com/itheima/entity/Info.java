package com.itheima.entity;

import com.itheima.pojo.Record;
import com.itheima.pojo.Repo;
import com.itheima.utils.Task;
import lombok.Data;

import java.io.*;
import java.util.*;

@Data
public class Info {
    private List<Repo> data;
    private TreeMap<Integer, Repo> slowTaskMap = new TreeMap<>();
    private TreeMap<Integer, String> total = new TreeMap<>();
    //    private TreeMap<Integer, String> npmTaskStrMap = new TreeMap<>();
    private HashMap<Integer, Record> recordHashMap = new HashMap<>();

    private BufferedReader reader;
    private PrintWriter out;
    private String writer;
//    private String file;


    public List<Repo> getData() {
        return data;
    }

    public void setList(List<Repo> data) {
        this.data = data;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void initWriter(String writer) {
        this.writer = writer;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public HashMap<Integer, Record> getRecordHashMap() {
        return recordHashMap;
    }

    public void setRecordHashMap(HashMap<Integer, Record> recordHashMap) {
        this.recordHashMap = recordHashMap;
    }


    public synchronized void putSlowTask(int i, Repo npmPkg) {
        slowTaskMap.put(i, npmPkg);
    }

    public synchronized void putSlowTaskData(int i, Repo repo) {
        slowTaskMap.put(i, repo);
    }

    public synchronized Repo pollData(int i) {
        Repo s = slowTaskMap.get(i);
        if (s != null) {
            slowTaskMap.remove(i);
        }
        return s;
    }

    public synchronized Repo giveType2() {
        Map.Entry<Integer, Repo> entry = slowTaskMap.pollFirstEntry();
        if (entry != null) {
            return entry.getValue();
        }
        return null;
    }

    public synchronized void save(int i, String s) {
        total.put(i, s);
    }

    public synchronized void saveToDB(int i, Record record) {
        recordHashMap.put(i, record);
    }

    public void raise() {
        if (data.size() == 0 && slowTaskMap.size() > 0) {
            Task.searchTask();
        }
    }

    public void saveToFile() throws FileNotFoundException {
        out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(writer, true))));

        Collection<String> values = total.values();
        for (String value : values) {
            out.write(value);
            out.write("\n");
        }
        out.close();
        System.out.println("All results in info are saved. ");
    }

    public Repo poll() {
        Repo repo = null;
        if (data.size() > 0) {
            repo = data.get(0);
            data.remove(0);
        }
        return repo;
    }

//    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
//
//        String range = "20001_21001";
//        String fromFilePath = "D:\\temp\\copied\\copied"+range+".txt";
//        Info info = new Info();
//        int startLine = 1;
//        int endLine = 1000;
//        int num = endLine - startLine+1;
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(fromFilePath));
//            Task.toLine(reader, startLine);
//            info.setReader(reader);
//            info.sort(num);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String s = null;
//        for (int i = 0; i < 1000; i++) {
//            System.out.println(info.poll());
//        }
//
//        long endTime = System.currentTimeMillis();
//        System.out.println(endTime - startTime + " ms, time for task");
//    }
//
//    public void setOut(PrintWriter out) {
//        this.out = out;
//    }
}
