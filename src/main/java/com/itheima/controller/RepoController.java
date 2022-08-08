package com.itheima.controller;

import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Repo;
import com.itheima.service.RepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.websocket.server.PathParam;

/**
 * @author v-rr
 */
@Controller
@RequestMapping("/repo")
public class RepoController {

    @Autowired
    private RepoService repoService;

    @ResponseBody
    @RequestMapping("/findPage")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = repoService.pageQuery(queryPageBean);
        return pageResult;
    }

    @ResponseBody
    @RequestMapping("/findById")
    public Result findById(@PathParam("id") Integer id) {

        Repo repo = null;
        try {
            repo = repoService.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "query repo failed");
        }
        return new Result(true, "success", repo);
    }


}
