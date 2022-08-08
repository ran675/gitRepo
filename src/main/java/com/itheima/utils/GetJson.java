package com.itheima.utils;

import java.util.*;

public class GetJson {
    private int index = 0;
    private int no = 0;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    private char[] chars;
    private LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();
    private LinkedHashMap<Integer, String> error = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Integer> line = new LinkedHashMap<>();

    public HashMap<String, Object> getJsonMap() {
        return jsonMap;
    }

    public void setJsonMap(LinkedHashMap<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public HashMap<Integer, String> getError() {
        return error;
    }

    public void setError(LinkedHashMap<Integer, String> error) {
        this.error = error;
    }

    public GetJson(String json) {
        this.chars = json.toCharArray();
    }

    public GetJson() {

    }

    public char[] getChars() {
        return chars;
    }

    public void setChars(char[] chars) {
        this.chars = chars;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean inBlank(char ch) {
        if (ch == '\n') {
            no++;
            line.put(no, index);
            return true;
        }
        return ch == '\t' || ch == ' ' || ch == '\r';
    }

    public void skipBlank() {
        while (inBlank(chars[index])) {
            index++;
        }
    }

    public String appendStr() {
        if (chars[index] != '\"' || index >= chars.length - 1) {
            return null;
        }
        index++;
        int temp = index;
        findNextCh('\"');
        skipBackslash('\"');
        char[] dest = new char[index - temp];
        System.arraycopy(chars, temp, dest, 0, index - temp);
        return new String(dest);
    }

    public void skipChAndBlank(char ch) {
        if (chars[index] == ',') {
            index++;
            skipBlank();
        }
    }

    public void findNextCh(char ch) {
        while (chars[index] != ch) {
            index++;
        }
    }

    public boolean isStopMark(char ch) {
        return ch == ']' || ch == '}' || ch == ',';
    }

    public boolean isBeginMark(char ch) {
        return ch == '{' || ch == '[' || ch == '\"';
    }

    public boolean CheckBeginMark() {
        if (!isBeginMark(chars[index])) {
            error.put(index, "chars " + index + "=" + chars[index] + " is not beginMark. ");
            return false;
        }
        return true;
    }

    public String appendItemToEndMark() {  // 不倒退，遇到结束的 逗号， ],  }
        int temp = index;
        char finalChar = chars[chars.length - 1];  // 使用哨兵，减少循环判断条件
        if (finalChar != '}') {
            chars[chars.length - 1] = '}';
        }
        while (!isStopMark(chars[index])) {
            index++;
        }
        if (index <= chars.length) {
            chars[chars.length - 1] = finalChar;
        }
        skipBackslash(chars[index]);
        char[] dest = new char[index - temp];
        System.arraycopy(chars, temp, dest, 0, index - temp);
        return new String(dest);
    }

    public void CheckAndSkipColon() {
        skipBlank();
        if (chars[index] == ':') {
            index++;
            skipBlank();
        } else {
            String s = "chars[" + index + "]=" + chars[index] + "  , is not colon :, error. ";
            error.put(index, s);
            System.out.println(s);
        }
    }

    public void putTo(Map map, String key, char ch) {
        if (ch == '\"') {
            String value = appendStr();  //  留在右边引号
            map.put(key, value);
        } else if (ch == '{') {
            HashMap<String, Object> map1 = new HashMap<>();
            formMap(map1);
            map.put(key, map1);
        } else if (ch == '[') {
            ArrayList list = new ArrayList<>();
            formList(list);
            map.put(key, list);
        }
    }

    public void putNextKvp(Map map) {  // begin with \"
        String key;
        key = appendStr();
        index++;
        CheckAndSkipColon();
        String value;
        char ch = chars[index];
        if (isBeginMark(ch)) {
            putTo(map, key, ch);
        } else if (ch != '}') {
            value = appendItemToEndMark();
            if (value.equals("true")) {
                map.put(key, true);
            } else if (value.equals("false")) {
                map.put(key, false);
            } else {
                map.put(key, value);
            }
//            留在右边逗号之前
            index--;
        }

    } // end with \"  ， }， 】 its own endMark;   the next char is ,  or  }  ;

    public void addNextValue(List list) {   // begin with \"
        String value;
        value = appendStr();
        list.add(value);
    }

    public void addTo(List list, char ch) {  // end at list sub item end mark;
        if (ch == '\"') {
            addNextValue(list);
        } else if (ch == '{') {
            HashMap<String, Object> m = new HashMap<>();
            formMap(m);
            list.add(m);
        } else if (ch == '[') {
            ArrayList<Object> list1 = new ArrayList<>();  /////
            formList(list1);
            list.add(list1);
        }
    }

    public void back(int i) {
        index -= i;
    }

    public void addNextVariousValue(List list) {  // begin with { ,  [ , \"
        char ch = chars[index];
        if (isBeginMark(ch)) {
            addTo(list, ch);
        } else if (ch != ']') {
            String value = appendItemToEndMark();
            list.add(value);
            index--;
//            留在右边逗号之前；
        }

    }// end with index after the right one; ready for next obj;  , or ];

    public void formMap(Map map) {  // // begin with  {
        index++;
        skipBlank();
        if (chars[index] == '}') {
            return;
        }
        if (!CheckBeginMark()) {
            return;
        }
        while (chars[index] != '}') {
            putNextKvp(map);  //!(chars[index]==','|| chars[index]=='}')
            index++;
            skipBlank();  ////
            if (!checkEnd(',', '}')) {
                return;
            }
            skipChAndBlank(',');
        }
    }  // end with } ;  its own end mark;

    public void formList(List list) {  // begin with [
        index++;
        skipBlank();
        if (chars[index] == ']') {
            return;
        }
        while (chars[index] != ']') {
            addNextVariousValue(list);
            index++;
            skipBlank();
            if (!checkEnd(',', ']')) {
                return;
            }
            skipChAndBlank(',');
        }
    } // end with ] ; of its same level;

    public void skipBackslash(char ch) {  // 从 “  }， 】 等等开始；
        if (chars[index - 1] == '\\') {
            index++;
            findNextCh(ch);
            if (chars[index - 1] == '\\') {
                skipBackslash(ch);
            }
        }
    }

    public void sortMap(Map map) {
        index = 0;
        findNextCh('{');
        formMap(map);
    }

    public void sortMap() {
        index = 0;
        findNextCh('{');
        formMap(this.jsonMap);
    }

    public boolean checkEnd(char expect, char end) {
        if (index > chars.length - 1) {
            return false;
        }
        if (chars[index] != expect && chars[index] != end) {
            String s = expect + " or " + end;
            error.put(index, s);
            return false;
        }
        return true;
    }

}