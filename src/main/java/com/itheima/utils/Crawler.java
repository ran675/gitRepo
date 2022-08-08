package com.itheima.utils;

import com.itheima.entity.Info;
import com.itheima.pojo.Record;
import com.itheima.pojo.Repo;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Crawler implements Runnable {

//    @Autowired
//    static RecordMapper recordMapper;

    static ArrayList<String> target = new ArrayList<>(Arrays.asList("[title=\"package.json\"]", "[title=\"packages\"]", "strong a[data-pjax=\"#repo-content-pjax-container\"]"));
    static ArrayList<String> textSelList = new ArrayList<>(Arrays.asList("table", "summary span[class=\"css-truncate-target\"]"));
    static ArrayList<String> dirSelList = new ArrayList<>(Arrays.asList("span[class=\"css-truncate css-truncate-target d-block width-fit\"] a[class=\"js-navigation-open Link--primary\"]"));

    static Map<String, Strategy> srcR = new HashMap<>(1024);
    static Map<Integer, Repo> current = new HashMap<>(16);

    private Repo repo = null;
    private Record record = null;
    private String rawNpmPkg = null;
    private HashMap<Integer, Data> d = new HashMap<>();
    private int way = -1;
    private StringBuffer comments = new StringBuffer();
    ;  //必须方便看到的信息comments; 必须指向 对应序号的comments；

    private static PrintWriter out;
    private static String toFileDirectory;
    private static Info info;
    private static int taskType = 1;
    static Integer planId;
    static int strategyCount = 9;
    static int cellLength = 50;
    int strType = -1;
    int searchSize = 0;
    //    String branch = null;
    String packagesHref = null;
    String jsonHref = null;
    String redirectLocation = null;
    String num = null;
    Strategy strategy = null;
    String npmShow = null;
    private boolean bySrcCommit = false;
    private boolean doSearch = false;
    CloseableHttpClient httpClient;
    //    HttpClientContext context ;
    static PoolingHttpClientConnectionManager cm;
    static RequestConfig config;

    public static void setInfo(Info info) {
        Crawler.info = info;
    }

    public static Info getInfo() {
        return info;
    }

    public static int getTaskType() {
        return taskType;
    }

    public static void setTaskType(int taskType) {
        Crawler.taskType = taskType;
    }

    public static Integer getPlanId() {
        return planId;
    }

    public static void setPlanId(Integer planId) {
        Crawler.planId = planId;
    }

    public synchronized void putSrcR(String s, Strategy s1) {
        srcR.put(s, s1);
    }

    class Strategy {
        String pre;
        String branch;
        String compare;
        int strategyType = -2;
        boolean packagesHref = true;
        boolean mono;
        boolean first = false;
    }

    @lombok.Data
    class Result {
        boolean flag;
        String val;
        StringBuffer sb = new StringBuffer();
    }

    class Data {
        String request = null;
        String entity = null;
        Document doc = null;
        String json = null;
        StringBuffer comments = new StringBuffer();
        GetJson getJson = null;
        String name = null;
        String repoUrl = null;
        String repoKeyUrl = null;
        String srcPath = null;
        int statusCode = 0;
        boolean sameName = false;
        boolean keyOK = false;
        HashMap<String, String> map = new HashMap<>();

        void select(String s) {
            Elements select = doc.select(s);
            if (select == null || select.size() < 1) {
                return;
            }
            String text = select.first().text();
            map.put(s, text);
        }

        void selectAttr(List<String> list) {
            for (String s : list) {
                Elements select = doc.select(s);
                if (select == null || select.size() < 1) {
                    continue;
                }
//            String text = select.first().text();
                String href = select.first().attr("href");
                if (href != null) {
                    try {
                        href = URLDecoder.decode(href, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                map.put(s, href);
            }
        }

        Elements selectDir(List<String> list) {
            String s = list.get(0);
            return doc.select(s);
        }

        String show(String str) {
//            name = String.format("%-16s", name);
            int length = Crawler.cellLength;
            char[] chars = new char[length];
            if (str == null) {
                str = "";
            }
            int i = str.length();
            if (length > i) {//                builder.append(str).append( new char[Crawler.cellLength - str.length()]);
                return String.format("%-" + length + "s", str);
            } else if (length == i) {
                return str;
            } else {//                builder.append(str, 0, Crawler.cellLength);
                System.arraycopy(str.toCharArray(), 0, chars, 0, length);
                chars[length - 1] = '.';
                chars[length - 2] = '.';
                chars[length - 3] = ' ';
                return new String(chars);
            }
        }

        StringBuffer showField(String... args) {
            StringBuffer s = new StringBuffer();
            for (String arg : args) {
                s.append(show(arg)).append("|  ");   //////
            }
            return s;
        }

        StringBuffer print() {  //,jsonHref,packagesHref,
            return showField(repo.getName(), name, repoUrl, repoKeyUrl);
        }

        StringBuffer append(Object... args) {
            for (Object arg : args) {
                comments.append(arg);
            }
            return comments;
        }

        StringBuffer appendField(Object... args) {
            StringBuffer s = new StringBuffer();
            for (Object arg : args) {
                s.append(blank(arg)).append('\t');
            }
            return s;
        }

        public void sort() {
            getJson.setChars(json.toCharArray());
            try {
                getJson.sortMap(getJson.getJsonMap());
            } catch (Exception e) {
                append("Json Map Error");
                e.printStackTrace();
                return;
            }
            getJson.getJsonMap().size();
        }

        public Object blank(Object s) {
            if (s == null) {
                return "";
            }
            return s;
        }

        public StringBuffer conclude() {  //  因为  @的名字被识别为公式无法正常显示  加一个空格
            HashMap<String, Object> jsonMap = getJson.getJsonMap();
            record.setDirectory(srcPath);
            record.setUrl(request);
            String commitId = getSub(request, "https://github.com/(.+?)/(blob||tree)/(.+?)/(.+?)/package.json", 2);
            record.setSrcCommit(commitId);
            if (repoKeyUrl != null) {
                record.setGit(repoKeyUrl);
            } else {
                record.setGit(repoUrl);
            }
            record.setUrl(request);
            try {
                record.setVersion((String) jsonMap.get("version"));
                Boolean aPrivate = (Boolean) jsonMap.get("private");
                if (aPrivate != null && aPrivate) {
                    record.setPrivacy("Y");
                }
            } catch (Exception e) {
                e.printStackTrace();
                record.setUrl("error");
            }
            record.setComments(comments.toString());
            saveRecord(comments.toString());
            comments.append('\t').append(appendField(srcPath, ' ' + name, request, repoUrl, repoKeyUrl));
            return comments;
        }
    }

    Data getDa() {
        return d.get(way);
    }

    void putData() {
        Data data2 = new Data();
        d.put(way, data2);
    }

    void putData(String uri) {
        Data data2 = new Data();
        data2.request = uri;
        d.put(way, data2);
    }

    String getRequest(Object... args) {
        StringBuilder address = new StringBuilder("https://github.com");
        for (Object arg : args) {
            address.append(arg);
        }
        return address.toString();
    }

    public String compareSrcRepo(String srcRepo, String newPath) {
        String s = "";
        if (newPath != null) {
            if (!newPath.contains(srcRepo)) {
                Pattern pattern = Pattern.compile(srcRepo, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(newPath);
                boolean find = false;
                while (matcher.find()) {
                    find = true;
                    break;
                }
                if (!find) {                //        String addr = "https://github.com/";
                    return "redirect to new repo " + newPath + "  ";
                }
            } // 完全一样则 不评论；
        } else {
            System.out.println("newPath = ?");
            return "newPath = ?";
        }
        return null;
    }

    int browse() {
        Data data = getDa();
        HttpGet httpGet = new HttpGet(data.request);
        httpGet.setConfig(config);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);   //需要处理不同响应码。
            data.statusCode = response.getStatusLine().getStatusCode();
            if (data.statusCode == 200) {
                HttpEntity httpEntity = response.getEntity();
                data.entity = EntityUtils.toString(httpEntity, "utf8");
                data.doc = Jsoup.parse(data.entity);
            } else {
                data.append(way, " code=", data.statusCode, " ");  //此处已经有了呀！！
                System.out.println(this.num + "  " + data.comments + "  " + data.request);
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data.statusCode;
    }

    Result checkGetName(String getName) {
        Result result = new Result();
        if (getName != null) {
            if (getName.equalsIgnoreCase(repo.getName())) {
                result.setFlag(true);
            } else {
                result.setVal("different name = " + getName + "  ");
                result.setFlag(false);
            }
        } else {
            result.setFlag(false);
            result.setVal("NameKey = null. ");//也有可能 有人不写 name;
        }
        return result;
    }

    void storeJson(int browse) {
        if (browse == 200) {
            storeJson();
        }
    }

    void storeJson() {
        Data data = getDa();
        data.select(textSelList.get(0));
        String tableSel = textSelList.get(0);
        String table = data.map.get(tableSel);
        if (table != null) {
            data.json = table;
            data.getJson = new GetJson();
            data.sort();
            HashMap<String, Object> jsonMap = data.getJson.getJsonMap();
//            if(data.getJson.getError().size()>0){
//                footnote.append(data.getJson.getError());
//            }
            String getName = (String) jsonMap.get("name");
            data.name = getName;
            Result result = checkGetName(getName);
            data.sameName = result.isFlag();
            if (!result.isFlag()) {
                data.append(result.val);
            } else {
                Object repository = jsonMap.get("repository");
                String url = null;
                if (repository == null) {
                    data.append("repository = ? ");
                } else if (repository instanceof Map) {
                    url = (String) ((Map) repository).get("url");
                    data.repoUrl = url;
                } else if (repository instanceof String) {
                    url = (String) repository;
                    data.repoKeyUrl = url;
                }
                if (!valid(url)) {
                    data.append("url = ? ");
                } else {
                    data.keyOK = true;
                }
            }

        } else {
            data.sameName = false;
            data.append("Repository Table Exception. ");
        }
        System.out.println(num + "-----\t-----\t" + way + "-----\tsameName = " + data.sameName + "-----\t" + data.request);
        System.out.println(data.print());
    }

    public StringBuilder npmShow() {
        String cmd = "cmd /c npm show " + repo.getName() + " repository";
        return runCMD(cmd).append(">>").append(cmd).append("  ");
    }

    String borrow() {
        String name1 = repo.getName();
        if (!name1.contains("/")) {
            return null;
        }
        String prefix1 = name1.split("/")[0];
        for (Repo value : current.values()) {
            String name = value.getName();
            String prefix = name.split("/")[0];
            if (prefix.equals(prefix1)) {
                if (valid(value.getSrcRepo())) {
                    Strategy strategy = srcR.get(value.getSrcRepo());
                    if (strategy != null && strategy.pre != null) {
                        redirectLocation = strategy.pre;
                        return strategy.pre;
                    }
                }
            }
        }
        return null;
    }


    synchronized void quit() {
        current.remove(repo.getId() - 6);
    }

    String repoToStr(Repo repo) {
        String s = "";
        Field[] fields = repo.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                s += field.get(repo) + "\t";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    void repo2Record(Repo repo, Record record) {
        record.setRepoId(repo.getId());
        record.setName(repo.getName());
        record.setSrcRepo(repo.getSrcRepo());
        record.setSrcCommit(repo.getSrcCommit());
        record.setVersion(repo.getVersion());
        record.setPlanId(planId);
    }


    boolean check() {  // 检查已经有数据库中的实体了。
        if (taskType == 1) {
            this.repo = info.poll();
            if (this.repo != null) {
                rawNpmPkg = repoToStr(repo);
                try {
//                    repo.attr(rawNpmPkg);
                    synchronized (this) {
                        current.put(Integer.valueOf(repo.getId()), repo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                repo.setSrcRepo("abpframework/abp");
                String srcRepo = repo.getSrcRepo();
                String No = repo.getId().toString();
                int number = repo.getId();
                String inPre = repo.getExistInPre();
                if ("Y".equals(inPre)) {  // 可以不处理。简单提示的两种情况； 但还是得写入啊！！
                    String s = "inPre = yes";
                    System.out.println(No + "\t");
                    info.save(number, rawNpmPkg);
                    saveRecord(s);
                    writeNpmTo(out, null);
                } else if (!valid(srcRepo)) {  // 允许 情况重叠，但是只会跳过一次，不会一条数据写两次；
                    String borrow = borrow();
                    if (borrow == null) {
                        String s = "srcRepo = ?";
                        System.out.println(No + "\t" + s);
                        StringBuilder append = npmShow().append(s);
                        info.save(number, rawNpmPkg + '\t' + append.toString());
                        saveRecord(append.toString());

                        writeNpmTo(out, append.toString());
                    } else {
                        repo.setSrcRepo(borrow);
                        return true;
                    }
                } else {
                    return true;
                }
                return check();
            } else {
                System.out.println("Switching type. ");
                taskType = 2;
                Task.task.changeT();
//                Task.clearFac();
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                info.raise();
                return false;
            }
        } else {
            repo = info.giveType2();
            if (repo == null) {
                try {
                    info.saveToFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Info save exception");
                }
//                System.out.println("Quit in 8s. ");
                try {
                    System.out.println("Try to shut down task");  ///  在容器内部是没有用的
//                    Task.clearFac();
//                    Thread.sleep(2000);
                    Task.task.stop();
//                    Thread.sleep(8000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                System.exit(0);
                return false;
            }
        }
        return true;
    }

    public String findBySearch(String uri, String name) {  // 用过传空值的办法吗？  //  需要主机名；
        putData(uri);  //
        Data data = getDa();
        String request = null;
//        System.out.println(new Date());
        if (valid(uri)) { // 已经有路径，就不必再找
            request = uri;
        } else {
            request = getRequest(redirectLocation);
        }
        URIBuilder uriBuilder = null;
        CloseableHttpResponse response = null;
        try {
            uriBuilder = new URIBuilder(request + "/search");
//            if (name.contains("@")) {
//                name = name.substring(1)    ;
//            }
//            name = name.substring("@angular-".length());
            //设置参数
            uriBuilder.setParameter("q", name);
            //创建HttpGet对象，设置url访问地址
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setConfig(config);
//            //使用HttpClient发起请求，获取response
            response = httpClient.execute(httpGet);
            data.statusCode = response.getStatusLine().getStatusCode();
            //解析响应
            String link = null;
            int pages = 0;
            if (data.statusCode == 200) {
                data.entity = EntityUtils.toString(response.getEntity(), "utf8");
                link = dealPage(data);
                if (searchSize == 0) {
                    return null;
                }
                Elements h3 = data.doc.select("h3");
                int resultNum = getResultNum(h3);
                pages = resultNum % searchSize == 0 ? resultNum / searchSize : (resultNum / searchSize + 1);
                if (pages > 4) {//pages 不能太多，会爆炸
                    pages = 4;
                }
            } else {
                data.append(data.statusCode + " = search statusCode. ");
                return null;
            }
            if (link == null && pages > 0) {
                for (int i = 2; i <= pages; i++) {
                    uriBuilder.addParameter("p", String.valueOf(i));
                    httpGet.setURI(uriBuilder.build());
                    response = httpClient.execute(httpGet);
                    data.statusCode = response.getStatusLine().getStatusCode();
                    if (data.statusCode == 200) {
                        data.entity = EntityUtils.toString(response.getEntity(), "utf8");
                        link = dealPage(data);
                        if (link != null) {
                            break;
                        } else {
                            Thread.sleep(1000);
                        }
                    } else {
                        data.append(data.statusCode).append(" = search statusCode. ");
                        System.out.println(data.statusCode + " = search Code. " + num);
                    }
                }
            }
            return link;
        } catch (Exception e) {
            e.printStackTrace();
            data.append("Exception at search. ");
        } finally {            //关闭response
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void analyzeSearch() {
        String srcPath = getSrcPath(getDa().request);
        if (srcPath != null) {
            String[] split = srcPath.split("(/||-)");
        }
    }

    public boolean doSearch() {
        String name = repo.getName();
        way = -1;
        if (redirectLocation == null) {
            return false;
        }
        System.out.println(new Date() + "  searching  " + num);
        String bySearch;   //href="/basscss/addons/blob/3567d575ecf406554f00a19cb5e107936cc7da5e/modules/responsive-position/package.json">
        if (name.contains("/")) {
            bySearch = findBySearch(
                    getRequest(redirectLocation), name.split("/")[1]);
            if (bySearch == null && searchSize > 0) {
                bySearch = findBySearch(
                        getRequest(redirectLocation), name.substring(1));
            }
        } else {
            bySearch = findBySearch(
                    getRequest(redirectLocation), name);
        }

        if (bySearch != null) {
            try {
                bySearch = URLDecoder.decode(bySearch, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            strType = -1;
            strategy.strategyType = strType;
            String request3 = getRequest(bySearch);
            putData(request3);
            int browse = browse();
            doSearch = true;
            if (browse == 200) {
                storeJson();
                return true;
            } else {   //  其实能搜索到的， 很严格，大概是有效的；除非太忙被拒绝
                getDa().append(bySearch);
            }
        } else {
            putData();
            getDa().append("Searching no result. ");
            System.out.println("Searching no result. " + num);
        }
        return false;
    }

    public static Integer getResultNum(Elements h3) {
        String text = h3.text();
        if (!text.contains("code")) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < text.length(); i++) {
            if (chars[i] > '9' || chars[i] < '0') {
                continue;
            } else {
                while (chars[i] <= '9' && chars[i] >= '0') {
                    builder.append(chars[i]);
                    i++;
                }
                break;
            }
        }
        return Integer.valueOf(builder.toString());
    }

    public String dealPage(Data data) {
        Document doc = Jsoup.parse(data.entity);
        data.doc = doc;
        Elements elements1 = doc.select("div[class=\"hx_hit-code code-list-item d-flex py-4 code-list-item-public repo-specific\"]");
        searchSize = elements1.size();
        for (Element element : elements1) {
            Elements select = element.select("div[class=\"f4 text-normal\"]");
            String text = select.text();
            if (text.contains("/package.json")) {//                        System.out.println(href);
                Elements table = element.select("td[class=\"blob-code blob-code-inner\"]");
                GetJson getJson = new GetJson();
                getJson.setChars(table.text().toCharArray());
                HashMap<String, Object> jsonMap = getJson.getJsonMap();
                try {  // 如果凑巧没有{ { ，会完蛋的；
                    getJson.findNextCh('\"');
                    getJson.back(1);
                    getJson.formMap(jsonMap);
                } catch (Exception e) {
                    System.out.println("Map forming Exception. ");
//                            e.printStackTrace();
                }//                        System.out.println(jsonMap);
                String name1 = (String) jsonMap.get("name");
                if (repo.getName().equals(name1)) {  //  href="/basscss/addons/blob/3567d575ecf406554f00a19cb5e107936cc7da5e/modules/responsive-position/package.json"
                    String href = element.select("a").attr("href");   //
                    return href;
                } else {
                    continue;
                }
            }//            System.out.println(text);
        }
        return null;
    }

    public String formSuffix(int i) {
        String name = repo.getName();
        String suffix = null;
        if (name.contains("@")) {
            name = name.substring(1);
        }
        if (i == 0) {
            suffix = name;
        } else if (i == strategyCount - 1) {
            suffix = repo.getName();
        } else {
            if (name.contains("-")) {
                switch (i) {
                    case 1:
                        suffix = name.replaceFirst("-", "/");
                        break;
                    case 2:
                        suffix = name.substring(name.indexOf("-") + 1);
                        break; //  2和3可能重复；
                    case 3:
                        suffix = name.substring(name.indexOf("-") + 1).replaceFirst("-", "/");
                        break;  // 可能不会同时包含；
                }
            }
            if (name.contains("/")) {
                if (i == 4) {
                    suffix = name.replaceFirst("/", "-");
                } else if (i == 5) {
                    suffix = name.substring(name.indexOf("/") + 1);
                } else if (i == 6) {
                    String s = name.split("/")[1];
                    suffix = s.substring(s.indexOf("-") + 1);
                } else if (i == 7) {
                    String s = name.split("/")[0];
                    suffix = s.split("-")[0] + '-' + name.split("/")[1];
                }
            }
        }
//        System.out.println("suffix = " + suffix);
        return suffix;
    }

    public String branch() {
        String branch = null;
        String srcCommit = repo.getSrcCommit();
        if (srcCommit != null) {
            branch = srcCommit;
        }
        if (strategy.branch.length() != 10) {
            branch = strategy.branch;
        }
        return branch;
    }

    public int mono(List<String> list1, boolean skip) {
        int count = 0;
        Elements elements = getDa().selectDir(dirSelList);
        way = 2;
        for (Element element : elements) {
            for (int i = 0; i < list1.size(); i++) {
                String suffix = list1.get(i);
                if (suffix == null || (i == strType && skip)) {
                    continue;
                }
                if (element.text().equals(suffix)) {
                    putData(getRequest(packagesHref, '/', element.text(), "/package.json"));
                    int browse = browse();
                    count++;
                    storeJson(browse);
                    if (getDa().sameName) {
                        strType = i;
                        strategy.strategyType = strType;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public int mono(List<String> list1, String branch) {
        int count = 0;
        int browse = 0;
        for (int i = 0; i < list1.size(); i++) {
            String suffix = list1.get(i);
            if (suffix == null || i == strType) {
                continue;
            }
            count++;
            packagesHref = redirectLocation + "/blob/" + branch + "/packages";
            putData(getRequest(packagesHref, '/', suffix, "/package.json"));
            browse = browse();
            storeJson(browse);
            if (getDa().sameName) {
                strType = i;
                strategy.strategyType = strType;
                break;
            } else {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return count;
    }

    public void requestMono() {
        way = 2;
        putData();
        int count = 0;
        String branch = branch();
        if (branch == null) {
            branch = "main";
        }
        String srcCommit = repo.getSrcCommit();
        packagesHref = redirectLocation + "/blob/" + branch + "/packages";  //  如果留在主分支没有用怎么办？
//        packagesHref = list.get(0);
        int browse = 0;
        if (strType > -1 && strType < strategyCount) {
            if (branch.equals(srcCommit)) {
                bySrcCommit = true;
            }
            String s = formSuffix(strType);
            if (s != null) {
                putData(getRequest(packagesHref, '/', s, "/package.json"));
                browse = browse();
                storeJson(browse);
            }
        }
        int code = 0;
        if (!getDa().sameName) {
            way = -3;
            code = getCommitPackHref(branch);
            List<String> list1 = new ArrayList<>();
            for (int i = 0; i < strategyCount; i++) {
                String s = formSuffix(i);
                if (!list1.contains(s)) {
                    list1.add(s);
                } else {
                    list1.add(null);
                }
            }

            if (code == 200) {
                count += mono(list1, true);
            }

            way = 2;
            if ((code != 200 || !getDa().sameName) && !bySrcCommit) {
                way = -5;
                packagesHref = redirectLocation + "/blob/" + srcCommit + "/packages";
                code = getCommitPackHref(srcCommit);
                if (code == 200) {
                    count += mono(list1, false);
                }
            }
            way = 2;
        } else {
            count++;
        }
        if (count == 0) {
            getDa().append("Not found. ");
            System.out.println("No available request. " + num);
        } else {
            if (!getDa().sameName && getDa().comments.length() < 1) {
                getDa().append("Not found. ");
            }
        }
    }

    public String addCommit(String name) {
        String srcCommit = repo.getSrcCommit();
        if (redirectLocation == null) {
            redirectLocation = '/' + repo.getSrcRepo();
        }
        if (name != null) {
            name = getRequest(redirectLocation, "/blob/", srcCommit, "/", name, "/package.json");
        } else {
            name = getRequest(redirectLocation, "/blob/", srcCommit, "/package.json");
        }
        return name;
    }

    //    不要分开，仍然是同样的方法；提高代码利用；run 方法内部加入其他操作；
    private boolean decide() {
        if (taskType == 1) {
            putData();
            addToSearch();
        } else if (taskType == 2) {
//            StringBuffer footnote = repo.getFootnote();
            putData();
            doSearch = doSearch();
            if (!getDa().sameName) {
//                data().append(footnote);
                if (strategy.mono) {   //  非mono repo 也能给directory？
                    runByNpm();
                    if (!getDa().sameName) {
                        way = -1;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void addToSearch() {
        strategy.pre = redirectLocation;
        if (!strategy.mono && srcR.get(repo.getSrcRepo()) == null) {  //  mono repo 在别的地方加入； 不建议把mono的策略用-1 覆盖；
            strategy.strategyType = -1;
            putSrcR(repo.getSrcRepo(), strategy);
        }
//    简单的pojo类不支持复杂操作？
//    repo.setFootnote(data().comments );  ///   重要， 把之前的总结内容保存到后面； 走 code!=200的判断路径
        int num = repo.getId();
        info.putSlowTask(num, repo);
        info.putSlowTaskData(num, repo);
        System.out.println("Add to later map , task " + num);
        quit();
    }

    public void saveRecord(String comments) {
        repo2Record(repo, record);
        record.setTime(POIUtils.getDate2ndTime());
        record.setComments(comments);
        info.saveToDB(repo.getId(), record);  //可选择批处理，也可以单条插入？
    }

    public void force(String srcRepo, String name) {
        way = 2;
        String request = getRequest(srcRepo, "/blob/", repo.getSrcCommit(), repo.getSrcPath(), "/package.json");
        System.out.println("request = " + request);
        putData(request);
        int browse = browse();
        storeJson(browse);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
//        System.out.println(Thread.currentThread());
        this.record = new Record();
        boolean check = check();//          repo.attr(rawNpmPkg);
        if (!check) {
            return;
        }
//        System.out.println(repo.toString());

        String srcRepo = repo.getSrcRepo();
        strategy = srcR.get(srcRepo);
        String No = repo.getId().toString();
        String name = repo.getName();
        String srcCommit = repo.getSrcCommit();
        this.num = No;
        System.out.println(num + "-----\t-----\t Task Start " + new Date());
        try {
            httpClient = HttpClients.custom().setConnectionManager(cm).build();
//            context = HttpClientContext.create();
            if (strategy != null && strategy.pre != null) {
//                if (strategy!=null&&strategy.pre!=null) {
                redirectLocation = strategy.pre;
                strType = strategy.strategyType;
//                System.out.println("previousLoc = " + previousLoc);
                if (strategy.mono) {
                    if (taskType == 1) {
                        requestMono();
//                        force(redirectLocation,name);
                    }
                }
                if (getDa() == null || !getDa().sameName) {
                    way = -1;
                    strType = -1;
                    putData();
                }
                if (strType == -1) {
                    boolean decide = decide();
                    if (!decide) {
                        return;
                    }
                }
            } else {  //  能到这边的都是 taskType ==1 的；不要decide;直接
                strategy = new Strategy();
                way = 0;
                putData(getRequest('/', srcRepo));
                int link = getLink();//            String jsonSel = target.get(0);    //            String packagesSel = target.get(1);
                if (link != 200) {  //  || (jsonHref==null&&packagesHref==null)
                    StringBuilder builder = npmShow().append(" Main dir ").append(link);
                    writeNpmTo(out, builder);
                    info.save(repo.getId(), rawNpmPkg + '\t' + builder);
                    saveRecord(builder.toString());

                    quit();
                    return;
                }
                if (!linkValid()) {   //  此外可以直接注释掉；包含了mono Repo 的请求
                    quit();
                    return;
                } else if (packagesHref == null) { //  common repo   //  需要把有名字中的@， 处理一下多个策略；   //  直接暴力拼接；
//                    jsonHref = redirectLocation + "/blob/" +  strategy.branch+ "/sdk/keyvault/" +name.split("/")[1]+ "/package.json";
//                    String request = getRequest(redirectLocation, "/blob/", repo.getSrcCommit(),repo.getSrcPath(), "/package.json");
//                    String s = name.split("/")[1];
//                    String s1 = s.substring(s.indexOf("-")+1);
                    way = 1;
//                    putData( request);
                    putData(getRequest(jsonHref));
                    int browse = browse();
                    storeJson(browse);
                    if (!getDa().sameName) {
                        if (!bySrcCommit && valid(srcCommit)) {
                            String req = addCommit(null);
//                            String req = data().request .replace("/master", "/"+srcCommit);
                            way = 7;
                            putData(req);
                            int browse1 = browse();
                            storeJson(browse1);
                            if (!getDa().sameName) {
                                way = 1;
                            }
                        }
                    }
                    if (!getDa().sameName) {
                        addToSearch();
                        return;
                    }
                }
            }
            if (way > 0 || way == -1) {
                Data data1 = getDa();
                if (data1.sameName) {  //                      srcR.put(srcRepo, newSrcPath);   previousLink 不带主机地址；
                    String request = data1.request;
                    String s1 = strategy.compare;
                    if (s1 != null) {
                        data1.append(s1);
                    }
                    if (name.contains("/")) {
                        name = name.replace("/", "-----");
                    }
                    String s = No + "_" + name + "_" + way;
                    saveFile(data1.json, toFileDirectory, "\\", s, ".txt");
                    saveFile(data1.entity, toFileDirectory, "\\Entity\\", s, ".html");
                    saveSerialized(data1.getJson.getJsonMap(), toFileDirectory, "\\Serialized\\", s, ".txt");
                    if (packagesHref != null) {
                        data1.append("Mono repo. ");
                    }
                    String srcPath = getSrcPath(request);
                    if (srcPath != null) {
                        data1.srcPath = '/' + srcPath;
                    }
                    if (!data1.keyOK) {
                        if (npmShow == null) {
                            npmShow = npmShow().toString();
                        }
                        data1.append(npmShow);
                    }
                    comments = data1.conclude();
                } else {  //  status Code != 200;  根本就没有搜索过， 也可以走这条路；
                    if (packagesHref != null) {
                        comments.append(" Mono repo ");
                    }
                    if (npmShow == null) {
                        npmShow = npmShow().toString();
                    }
                    comments.append(npmShow);
                    String s1 = strategy.compare;
                    if (s1 != null) {
                        comments.append(s1);
                    }
                    comments.append(data1.comments);
                }
            }
        } catch (Exception e) {
            comments.append("Runtime Exception. ");
            saveRecord(comments.toString());
            e.printStackTrace();
        }
        info.save(Integer.parseInt(No), rawNpmPkg + '\t' + comments.toString());

        writeNpmTo(out, comments.toString());
        long endTime = System.currentTimeMillis();
        System.out.println(num + "\t" + (endTime - startTime) + " ms. " + "-----\t-----\tEnd");
    }

    private void runByNpm() {
        StringBuilder stringBuilder = npmShow();
        npmShow = stringBuilder.toString();
        if (npmShow.contains("directory")) {
            String s = getSub(npmShow, "(.+)directory: '(.+?)'(.+?)", 2);
            System.out.println("directory = " + s);
            String request = getRequest(redirectLocation, "/blob/", strategy.branch, "/", s, "/package.json");
            way = 100;
            putData(request);
            int browse = browse();
            storeJson(browse);
            if (getDa().sameName) {
                strategy.strategyType = strategyCount + 1;
            }
        }
    }

    private boolean linkValid() {
        if (jsonHref == null && packagesHref == null) {
            getDa().append("No json, no packages/. ");
            strategy.pre = redirectLocation;
            addToSearch();
            return false;
        } else if (packagesHref != null) {
            strategy.mono = true;    // 明显的mono Repo 很好处理；
            requestMono();
//            force(redirectLocation,repo.getName());
            if (redirectLocation != null) {
                strategy.pre = redirectLocation;
                putSrcR(repo.getSrcRepo(), strategy);
            }  //  packagesHref  =  /microsoft/backfill/tree/master/packages
            strategy.first = true;
            if (!getDa().sameName) {
                addToSearch();
                return false;
            } else {
                return true;
            }
        }  //  直接暴力拼接；
        return true;
    }

    private String getSrcPath(String request) {
        return getSub(request, "https://github.com/(.+?)/(blob||tree)/(.+?)/(.+?)/package.json", 4);
    }

    public String getSub(String URI, String reg, int i) {
        if (URI != null) {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(URI);
            if (matcher.find()) {
                return matcher.group(i);
            }
        }
        return null;
    }

    public void saveFile(Object content, Object... args) {
        StringBuilder tofile = new StringBuilder();
        for (Object arg : args) {
            tofile.append(arg);
        }
        try {
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(tofile.toString(), true))));
            if (content != null) {
                out.write(content.toString());
            } else {
                System.out.println("File content Empty!!!  " + num);
            }
            out.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveSerialized(Object content, String... args) {
        if (content == null) {
            return;
        }
        StringBuilder tofile = new StringBuilder();
        for (String arg : args) {
            tofile.append(arg);
        }
        try {
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(tofile.toString(), true));
            output.writeObject(content);
            output.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean valid(String s) {
        return s != null && s.length() > 2;
    }

    public int getLink() {
        String jsonSel = target.get(0);
        String packagesSel = target.get(1);
        String mainDir = target.get(2);
        String branchSel = textSelList.get(1);
        Data data = getDa();
        int browse = browse();
        quit();
        if (browse == 200) {
            data.selectAttr(target);
            data.select(branchSel);
            jsonHref = data.map.get(jsonSel);
            packagesHref = data.map.get(packagesSel);
            redirectLocation = data.map.get(mainDir);
            strategy.branch = data.map.get(branchSel);
//            System.out.println( data.map.toString());

            if (redirectLocation == null) {
                if (repo.getSrcRepo() != null) {
                    redirectLocation = repo.getSrcRepo();
                } else {
                    return 4;
                }
            }

            String s = compareSrcRepo(repo.getSrcRepo(), redirectLocation);
            if (s != null) {
                strategy.compare = s;
            }
            if (valid(packagesHref)) {
                return browse;
            }
        } else {
            strategy.packagesHref = false;
            return browse;
        }
        if (jsonHref == null && packagesHref == null) {
            if (!valid(repo.getSrcCommit()) || bySrcCommit) {   ////
                return browse;
            }
            String request = addCommit(null);
            way = -2;
            putData(request);
            bySrcCommit = true;   // only Place ?
            int link = getLink();
            if (link != 200) {
                way = 0;
                return browse;
            } else {
                return link;
            }
        } else {
            if (bySrcCommit && packagesHref == null) {
                strategy.packagesHref = false;
            }
            return browse;
        }
    }

    public int getCommitPackHref(String srcCommit) {
        if (valid(srcCommit)) {
            putData(getRequest(redirectLocation, "/tree/", srcCommit, "/packages"));
            return browse();
        }
        return 0;
    }

    public synchronized void writeNpmTo(PrintWriter out, Object args) {
//        出了异常一定要有提示！！，不能留空白，写原来的东西；
        try {
            synchronized (this) {
                if (rawNpmPkg == null) {
                    rawNpmPkg = "Exception" + repo.getId();
                }
                out.write(rawNpmPkg);
                out.write("\t");
                if (args != null) {
                    out.write(args.toString());
                }
                out.write("\n");
                out.flush();
            }
            ;
        } catch (Exception e) {
            synchronized (this) {  // 这边很难会遇到；
                out.write("Exception");
                out.write(rawNpmPkg);
                out.write("\n");
                out.flush();
            }
            ;
            e.printStackTrace();
        }
    }

    public static StringBuilder runCMD(String cmd) {
        Process p = null;
        BufferedReader br = null;
        StringBuilder builder = new StringBuilder();
        try {
            p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String readLine = "";
            while ((readLine = br.readLine()) != null) {
                builder.append(readLine);  //                builder.append("\n");
            }
            System.out.println(builder.toString());
            p.waitFor(); //            int i = p.exitValue();
            return builder;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder;
    }

    public void clear() {
        repo = null;
        record = null;
        rawNpmPkg = null;
        npmShow = null;
        comments = new StringBuffer();
        ;  //必须方便看到的信息comments; 必须指向 对应序号的comments；
        d = new HashMap<>();
        packagesHref = null;
        jsonHref = null;
        redirectLocation = null;
        num = null;
        way = -1;
        bySrcCommit = false;
        searchSize = 0;
        strategy = null;
    }


    public static PrintWriter getOut() {
        return out;
    }

    public static void setOut(PrintWriter out) {
        Crawler.out = out;
    }

    public static String getToFileDirectory() {
        return toFileDirectory;
    }

    public static void setToFileDirectory(String toFileDirectory) {
        Crawler.toFileDirectory = toFileDirectory;
    }

    public static PoolingHttpClientConnectionManager getCm() {
        return cm;
    }

    public static void setCm(PoolingHttpClientConnectionManager cm) {
        Crawler.cm = cm;
    }

    public static RequestConfig getConfig() {
        return config;
    }

    public static void setConfig(RequestConfig config) {
        Crawler.config = config;
    }
}

