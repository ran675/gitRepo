package com.itheima.controller;

import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Plan;
import com.itheima.pojo.Record;
import com.itheima.pojo.Repo;
import com.itheima.service.PlanService;
import com.itheima.service.RecordService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author v-rr
 */
@RestController
@RequestMapping("/plan")
public class PlanController {
    public static boolean switchFlag = false;
    public static boolean endFlag = false;

    @Autowired
    private PlanService planService;

    @Autowired
    private RecordService recordService;

    @RequestMapping("/findPage")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = planService.pageQuery(queryPageBean);
        return pageResult;
    }

    @RequestMapping("/findById")
    public Result findById(Integer id) {

        Plan plan = null;
        try {
            plan = planService.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "query Plan failed");
        }
        return new Result(true, "success", plan);
    }

    boolean validStr(String s) {
        return s != null && s.length() > 0;
    }

    @RequestMapping("/add")
    public Result save(Integer[] repoIds, @RequestBody Plan plan) {
        try {
            if (!validStr(plan.getName()) && !validStr(plan.getStart().toString())) {
                return new Result(false, "Plan invalid !");
            }
            planService.save(plan);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "save Plan failed");
        }
        return new Result(true, "success");
    }

    @RequestMapping("/edit")
    public Result edit(Integer[] repoIds, @RequestBody Plan plan) {
        try {
            if (!validStr(plan.getName()) && !validStr(plan.getStart().toString())) {
                return new Result(false, "Plan invalid !");
            }
            planService.edit(plan);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "save Plan failed");
        }
        return new Result(true, "success");
    }

    @RequestMapping("/findRepos")
    public Result findRepos(@RequestBody Plan plan) {
        List<Repo> repoList;
        HashMap<String, Object> map = new HashMap<>();
        try {
            repoList = planService.findRepos(plan);
            map.put("list", repoList);
            List<Integer> list = new ArrayList<>();
            for (Repo repo : repoList) {
                list.add(repo.getId());
            }
            map.put("repoIds", list);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "fail to find repos");
        }
        return new Result(true, "success", map);
    }

    @RequestMapping("/delete")
    public Result delete(Integer id) {
        try {
            planService.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "fail to delete plan");
        }
        return new Result(true, "success");
    }


    @RequestMapping("/execute")
    public Result execute(Integer id) {
        try {
            planService.execute(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "fail to execute plan");
        }
        return new Result(true, "success");
    }

    @RequestMapping("/export")
    public Result exportRecords(HttpServletRequest request, HttpServletResponse response, @PathParam("id") Integer id) {
        try {
            HashMap<Integer, Record> export = planService.export(id);
            Plan plan = planService.findById(id);
//            String filePath = ClassUtils.getDefaultClassLoader().getResource(File.separator+"templates"+ File.separator+"report_template.xlsx").getPath();
            final String filePath = ClassUtils.getDefaultClassLoader().getResource("static/templates/report_template.xlsx").getPath();

//            String filePath = request.getSession().getServletContext().getRealPath("templates")+ File.separator+"report_template.xlsx";
            //基于提供的Excel模板文件在内存中创建一个Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(new FileInputStream(new File(filePath)));
            //读取第一个工作表
            XSSFSheet sheet = excel.getSheetAt(0);
            Field[] fields = Record.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }
            int count = 1;
            int i = 0;
            XSSFRow row = null;
            if (export == null || export.size() == 0) {
                List<Record> list = recordService.findByPlanId(id);

                for (Record record : list) {
                    row = sheet.createRow(count);
                    i = 0;
                    for (Field field : fields) {
                        if (field.get(record) != null) {
                            row.createCell(i).setCellValue(String.valueOf(field.get(record)));
                        }
                        i++;
                    }
                    count++;
                }
            } else {
                Set<Integer> set = export.keySet();
                for (Integer integer : set) {
                    Record record = export.get(integer);
                    row = sheet.createRow(count);
                    i = 0;
                    for (Field field : fields) {
                        if (field.get(record) != null) {
                            row.createCell(i).setCellValue(String.valueOf(field.get(record)));
                        }
                        i++;
                    }
                    count++;
                }
            }
            OutputStream out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");//代表的是Excel文件类型
            response.setHeader("content-Disposition", "attachment;filename=report_" + id + ".xlsx");//指定以附件形式进行下载
            excel.write(out);


            out.flush();
            out.close();
//            excel.close();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "export record failed");
        }
        return null;
    }

}
