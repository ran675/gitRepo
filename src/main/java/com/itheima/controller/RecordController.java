package com.itheima.controller;

import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Record;
import com.itheima.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @RequestMapping("findByRepoId")
    public Result findByRepoId(Integer id) {
        Set<Record> set = null;
        Record record = null;
        try {
            set = recordService.findByRepoId(id);
            for (Record record1 : set) {
                String url = record1.getUrl();
                if (url != null && url.length() > 0) {
                    record = record1;
                    return new Result(true, "success", record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "query repo failed");
        }
        return new Result(false, "query repo failed");
    }

    @RequestMapping("/findPage")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = recordService.pageQuery(queryPageBean);
        return pageResult;
    }

    @RequestMapping("/findById")
    public Result findById(Integer id) {

        Record record = null;
        try {
            record = recordService.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "query record failed");
        }
        return new Result(true, "success", record);
    }

    @RequestMapping("/edit")
    public Result edit(@RequestBody Record record) {
        try {
            if (record.getName() == null || record.getName().length() < 2) {
                return new Result(false, "edit record failed");
            }
            recordService.edit(record);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "edit record failed");
        }
        return new Result(true, "success");
    }

    @RequestMapping("/delete")
    public Result delete(Integer id) {
        try {
            recordService.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "edit record failed");
        }
        return new Result(true, "success");
    }


}
