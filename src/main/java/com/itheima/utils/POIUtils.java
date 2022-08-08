package com.itheima.utils;

import com.itheima.pojo.Repo;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class POIUtils {
    private final static String xls = "xls";
    private final static String xlsx = "xlsx";
    private final static String DATE_FORMAT = "yyyy/MM/dd";

    /**
     * 读入excel文件，解析后返回
     *
     * @param file
     * @throws IOException
     */
    public static List<String[]> readExcel(MultipartFile file) throws IOException {
        //检查文件
        checkFile(file);
        //获得Workbook工作薄对象
        Workbook workbook = getWorkBook(file);
        //创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
        List<String[]> list = new ArrayList<String[]>();
        if (workbook != null) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                //获得当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet == null) {
                    continue;
                }
                //获得当前sheet的开始行
                int firstRowNum = sheet.getFirstRowNum();
                //获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                //循环除了第一行的所有行
                for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
                    //获得当前行
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    //循环当前行
                    for (int col = firstCellNum; col < lastCellNum; col++) {
                        Cell cell = row.getCell(col);
                        cells[col] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
//            workbook.close();
        }
        return list;
    }

    //校验文件是否合法
    public static void checkFile(MultipartFile file) throws IOException {
        //判断文件是否存在
        if (null == file) {
            throw new FileNotFoundException("文件不存在！");
        }
        //获得文件名
        String fileName = file.getOriginalFilename();
        //判断文件是否是excel文件
        if (!fileName.endsWith(xls) && !fileName.endsWith(xlsx)) {
            throw new IOException(fileName + "不是excel文件");
        }
    }

    public static String getDateMinTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return format.format(new Date());
    }

    public static String getDate2ndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(new Date());
    }

    public static List<Repo> readBetweenLines(Workbook workbook, int start, int end) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Sheet sheet = workbook.getSheetAt(0);
        List<Repo> list = new ArrayList<>();
        Repo repo = null;
        if (start < 1 || start > end) return null;
        for (int i = start - 1; i < end; i++) {
            Row row = sheet.getRow(i);
            repo = new Repo();
            Field[] fields = repo.getClass().getDeclaredFields();
            try {
                fields[0].setAccessible(true);
                fields[0].set(repo, i + 1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < 10; j++) {
                String cellValue = getCellValue(row.getCell(j));
                try {
                    fields[j + 1].setAccessible(true);
                    fields[j + 1].set(repo, cellValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (row.getCell(5) != null) {
                    //                    System.out.println(getCellValue(row.getCell(5)));
                    double val = Double.valueOf(getCellValue(row.getCell(5)));
                    Date date = HSSFDateUtil.getJavaDate(val);
                    String s = format.format(date);
//                    System.out.println(s);
                    fields[6].set(repo, s);
                } else {
                    System.out.println("Time cell is NULL at row　" + i);
                }
                if (repo.getRepoKeyPresent().equalsIgnoreCase("yes")) {
                    repo.setRepoKeyPresent("Y");
                } else if (repo.getRepoKeyPresent().equalsIgnoreCase("no")) {
                    repo.setRepoKeyPresent("N");
                } else {
                    repo.setRepoKeyPresent("NA");
                }
                if (repo.getExistInPre().equalsIgnoreCase("yes")) {
                    repo.setExistInPre("Y");
                } else if (repo.getExistInPre().equalsIgnoreCase("no")) {
                    repo.setExistInPre("N");
                } else {
                    repo.setExistInPre("NA");
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            list.add(repo);
        }
        return list;
    }

//    public static Date doubleToDate(Double dateTime) {
//        Calendar base = Calendar.getInstance();
//        //Delphi的日期类型从1899-12-30 开始
//        base.set(1899, 11, 30, 0, 0, 0);
//        base.add(Calendar.DATE, dateTime.intValue());
//        base.add(Calendar.MILLISECOND,(int)((dateTime % 1) * 24 * 60 * 60 * 1000));
//        return base.getTime();
//    }


    public static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith(xls)) {
                //2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(xlsx)) {
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public static Workbook getWorkBook(File file) {
        String fileName = file.getName();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = new FileInputStream(file);
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith(xls)) {
                //2003
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(xlsx)) {
                //2007
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if (cell == null) {
            return cellValue;
        }
        //如果当前单元格内容为日期类型，需要特殊处理
        String dataFormatString = cell.getCellStyle().getDataFormatString();
        if (dataFormatString.equals("m/d/yy")) {
            cellValue = new SimpleDateFormat(DATE_FORMAT).format(cell.getDateCellValue());
            return cellValue;//"2019/10/10"
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case Cell.CELL_TYPE_BLANK: //空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }
}
