package com.itheima.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.mapper.RecordMapper;
import com.itheima.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RecordService {

    @Autowired
    private RecordMapper recordMapper;


    public Set<Record> findByRepoId(Integer id) {
        return recordMapper.findByRepoId(id);
    }


    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer pageSize = queryPageBean.getPageSize();
        Integer currentPage = queryPageBean.getCurrentPage();
//        System.out.println(pageSize);
        Page<Record> page = new Page<>(currentPage, pageSize, true);
        IPage<Record> repoIPage = recordMapper.selectPage(page, null);
        return new PageResult(repoIPage.getTotal(), repoIPage.getRecords());
    }

    public Record findById(Integer id) {
        return recordMapper.selectById(id);
    }

    public void edit(Record record) {
        recordMapper.updateById(record);
    }

    public void delete(Integer id) {
        recordMapper.deleteById(id);
    }

    public List<Record> findByPlanId(Integer id) {
        QueryWrapper<Record> wrapper = new QueryWrapper<>();
        wrapper.eq("planId", id);
        return recordMapper.selectList(wrapper);
    }
}
