package com.itheima.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author v-rr
 */
public class RanJson {

    private final UStack<LinkedHashMap<Object, Object>> stackM = new UStack<>();
    private final UStack<ArrayList<Object>> stackL = new UStack<>();
    private final UStack<Object> stackKey = new UStack<>();
    private final UStack<Integer> stackKeyLeastSize = new UStack<>();
    private final UStack<Boolean> stackIsMap = new UStack<>();
    private LinkedHashMap<Object, Object> resMap;
    private int index = 0;
    private int limit;
    private char[] chars;

    public HashMap parseJson(String source) {
        resMap = new LinkedHashMap<>();
        chars = source.toCharArray();
        boolean isMap = false;
        for (index = 0; index < chars.length; index++) {
            if (!isInvisibleChar(chars[index])) {
                if (chars[index] != '{') {
                    return resMap;
                } else {
                    stackM.push(resMap);
                    stackIsMap.push(true);
                    stackKeyLeastSize.push(0);
                    index++;
                    break;
                }
            }
        }
        for (limit = source.length() - 1; isInvisibleChar(chars[limit]); limit--) {
            ;
        }
        if (chars[limit] != '}') {
            formatter.append("Final char error not  } . ");
        }
        limit--;
        for (; index < limit; index++) {
            char c = chars[index];
            if (isInvisibleChar(c)) {
                continue;
            }
            if (c == '\"') {
                isMap = stackIsMap.top();
                if (isMap) {
                    String s = getStr();
                    if (stackKey.size() <= stackKeyLeastSize.top()) {
                        stackKey.push(s);
                    } else {
                        stackM.top().put(stackKey.pop(), s);
                    }
                } else {
                    ArrayList<Object> top = stackL.top();
                    String s = getStr();
                    top.add(s);
                }
            } else if (c == ':') {

            } else if (c == '{') {
                stackKeyLeastSize.push(stackKey.size());
                stackIsMap.push(true);
                stackM.push(new LinkedHashMap<>());
            } else if (c == '[') {
                stackKeyLeastSize.push(stackKey.size());
                stackIsMap.push(false);
                stackL.push(new ArrayList<>());
            } else if (c == '}') {
                LinkedHashMap<Object, Object> pop = stackM.pop();
                add(pop);
            } else if (c == ']') {
                ArrayList<Object> pop = stackL.pop();
                add(pop);
            } else if (c != ',') {  //                    这是Boolean
                isMap = stackIsMap.top();
                Object bool = getBool();
                if (isMap) {
                    if (stackKey.size() <= stackKeyLeastSize.top()) {
                        stackKey.push(bool);
                    } else {
                        stackM.top().put(stackKey.pop(), bool);
                    }
                } else {
                    ArrayList<Object> top = stackL.top();
                    top.add(bool);
                }
            }
        }
        stackM.pop();
        stackIsMap.pop();
        stackKeyLeastSize.pop();
        return resMap;
    }

    // 注意! true false 没有引号包裹 !
    private Object getBool() {
        String s;
        if (chars[index] == 'f' || chars[index] == 'F') {
            s = new String(chars, index, 5);
            if ("false".equalsIgnoreCase(s)) {
                index += 5;
                return false;
            }
        } else if (chars[index] == 't' || chars[index] == 'T') {
            s = new String(chars, index, 4);
            if ("true".equalsIgnoreCase(s)) {
                index += 4;
                return true;
            }
        } else {
            int j;
            for (j = index; index < limit && isDigit(chars[index]); index++) {
                ;
            }
            String v = new String(chars, j, index - j);
            try {  // 也有可能是其他 float decimal 类型数字 暂不考虑
                return Integer.valueOf(v);
            } catch (NumberFormatException e) {
                return v;
//                e.printStackTrace();
            }
        }
        return null;
    }

    private String getStr() {
        for (int j = index + 1; j < limit; j++) {
            if (chars[j] == '\"' && chars[j - 1] != '\\') {
                String s = new String(chars, index + 1, j - index - 1);
                index = j;
                return s;
            }
        }
        return null;
    }

    private StringBuffer formatter;
    private String occupier = "  ";

    public StringBuffer getFormatter() {
        return formatter;
    }

    public String getOccupier() {
        return occupier;
    }

    public void setOccupier(String occupier) {
        this.occupier = occupier;
    }

    public String getContent() {
        formatter = new StringBuffer();
        formatMap(resMap, "");
        return formatter.toString();
    }

    private void wrapStr(Object s) {
        formatter.append("\"").append(s).append("\"");
    }

    private void formatMap(HashMap<Object, Object> o, String prefix) {
        formatter.append("{");
        String s = prefix + occupier;
        Set<Map.Entry<Object, Object>> entries = o.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            formatter.append("\n").append(s);
            Object key = entry.getKey();
            if (key instanceof String) {
                wrapStr(key);
            } else {
                formatter.append(key);
            }
            formatter.append(": ");
            Object v = entry.getValue();
            addValStr(s, v);
        }
        if (o.size() > 0) {
            formatter.setCharAt(formatter.length() - 1, '\n');
            formatter.append(prefix);
        }
        formatter.append("}");
    }

    private void addValStr(String s, Object v) {
        if (v instanceof String) {
            wrapStr(v);
        } else if (v instanceof List) {
            formatList((List<Object>) v, s);
        } else if (v instanceof HashMap) {
            formatMap((HashMap<Object, Object>) v, s);
        } else {
            formatter.append(v);
        }
        formatter.append(",");
    }

    private void formatList(List<Object> o, String prefix) {
        formatter.append("[");
        String s = prefix + occupier;
        for (Object v : o) {
            formatter.append("\n");
            formatter.append(s);
            addValStr(s, v);
        }
        if (o.size() > 0) {
            formatter.setCharAt(formatter.length() - 1, '\n');
            formatter.append(prefix);
        }
        formatter.append("]");
    }

    private void add(Object list) {
        stackIsMap.pop();
        stackKeyLeastSize.pop();
        if (stackIsMap.top()) {
            if (stackKey.empty()) {
                stackKey.push(list);
            } else {
                stackM.top().put(stackKey.pop(), list);
            }
        } else {
            stackL.top().add(list);
        }
    }

    private boolean isInvisibleChar(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private boolean isEndSymbol(char c) {
        return c == ',' || c == '\n' || c == '\r' || c == ']' || c == '}' || c == '\"' || c == ' ' || c == ':';
    }

    private boolean isDigit(char c) {
        return c <= '9' && c >= '0';
    }

    public void add(boolean isMap, Object list) {
        if (isMap) {
            if (stackKey.empty()) {
                stackKey.push(list);
            } else {
                stackM.top().put(stackKey.pop(), list);
            }
        } else {
            stackL.top().add(list);
        }
    }


    public static StringBuffer read(File file) {
        StringBuffer buf = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                buf.append(reader.readLine());
                buf.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf;
    }

    public static StringBuffer read(String FileName) {
        return read(new File(FileName));
    }

    public String[] multiFormat(String[] source) {
        String[] res = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            StringBuffer buffer = read(source[i]);
            parseJson(String.valueOf(buffer));
            res[i] = getContent();
        }
        return res;
    }

    public static void main(String[] args) {
        String[] source = {
                "D:\\temp\\11001_12001\\11001_npm-cache-filename_json3.txt",
//                "D:\\temp\\11001_12001\\11015_npm_json3.txt",
//                "D:\\temp\\11001_12001\\11002_npm-bundled_json3.txt"
        };

        RanJson p = new RanJson();
        String[] strings = p.multiFormat(source);
        for (int i = 0; i < strings.length; i++) {
            System.out.println(i + "------------- ");
            System.out.println(strings[i]);
            System.out.println(i + "-----------end ");
        }

    }

}
