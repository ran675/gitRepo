package com.itheima.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.entity.Info;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.mapper.PlanMapper;
import com.itheima.mapper.RecordMapper;
import com.itheima.mapper.RepoMapper;
import com.itheima.pojo.Plan;
import com.itheima.pojo.Record;
import com.itheima.pojo.Repo;
import com.itheima.utils.Task;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service("planService")
@Transactional
public class PlanService {

    @Autowired
    private PlanMapper planMapper;
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private RepoMapper repoMapper;

    @Autowired
    private RedisTemplate<Serializable, Serializable> redisTemplate;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;//引入bean

    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer pageSize = queryPageBean.getPageSize();
        Integer currentPage = queryPageBean.getCurrentPage();
//        System.out.println(pageSize);
        Page<Plan> page = new Page<>(currentPage, pageSize, true);
        IPage<Plan> repoIPage = planMapper.selectPage(page, null);
        return new PageResult(repoIPage.getTotal(), repoIPage.getRecords());
    }

//    public PageResult pageQuery(QueryPageBean queryPageBean,String column) {
//        Integer pageSize = queryPageBean.getPageSize();
//        Integer currentPage = queryPageBean.getCurrentPage();
//        String queryString = queryPageBean.getQueryString();
//        QueryWrapper<Plan> wrapper = new QueryWrapper<>();
//        if (column!=null) {
//            wrapper.like(column, queryString);
//        }
//        IPage<Plan> page = new Page<>(currentPage,pageSize);
//        IPage<Plan> repoIPage = planMapper.selectPage(page, wrapper);
//
//
//        return new PageResult(repoIPage.getTotal(), repoIPage.getRecords());
//    }


    public Plan findById(Integer id) {
        return planMapper.selectById(id);
    }

    public void save(Plan plan) {
        String datetime = getDatetime();
        plan.setTime(datetime);
        planMapper.insert(plan);
    }

    public String getDatetime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.CHINA);
        return format.format(new Date());
    }

    public List<Repo> execute(Integer id) {
        ArrayList<Repo> list = null;
        try {
            ValueOperations<Serializable, Serializable> operations = redisTemplate.opsForValue();
            list = (ArrayList<Repo>) operations.get("repoPlanList" + id);
//            System.out.println("repoPlanList"+id);
//            System.out.println(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (list == null || list.size() == 0) {
            Plan plan = planMapper.selectById(id);
            list = (ArrayList<Repo>) findReposBetweenStartAndEnd(plan.getStart(), plan.getEnd());
        }
//        System.out.println(list);

//        Info info = Task.run(list, id);// 最好是在其他主机上运行，任务耗时而且容易阻塞// 会另行开线程执行task;

        updateExecuted(id, "E");
        return list;
    }

    public List<Repo> findReposBetweenStartAndEnd(Integer start, Integer end) {
        if (start == null) {
            return null;
        } else if (end == null) {
            end = start;
        }
        QueryWrapper<Repo> wrapper = new QueryWrapper<>();
        wrapper.between("id", start, end);

        return repoMapper.selectList(wrapper);
    }

    public void saveRecords(HashMap<Integer, Record> recordMap) {
        //        HashMap<Integer, Record> recordMap = info.getRecordMap();
        Iterator<Integer> iterator = recordMap.keySet().iterator();
        List<Record> records = new ArrayList<>();

        while (iterator.hasNext()) {
            records.add(recordMap.get(iterator.next()));
        }
        saveMsgItemList(records);

    }

    public void updateExecuted(Integer id, String value) {
        planMapper.updateExecuted(id, value);
    }

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public <T> void saveMsgItemList(List<T> list) {
        // 这里注意VALUES要用实体的变量，而不是字段的Column值
        String sql = "INSERT INTO t_record( repoId,planId, name,version,srcRepo,srcCommit," +
                "directory,git,url,comments,time,privacy )" +
                " values( :repoId,:planId, :name,:version,:srcRepo,:srcCommit," +
                ":directory,:git,:url,:comments,:time,:privacy )";
        updateBatchCore(sql, list);
    }

    /**
     * 一定要在jdbc url 加&rewriteBatchedStatements=true才能生效
     *
     * @param sql  自定义sql语句，类似于 "INSERT INTO chen_user(name,age) VALUES (:name,:age)"
     * @param list
     * @param <T>
     */
    public <T> void updateBatchCore(String sql, List<T> list) {
        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
        namedParameterJdbcTemplate.batchUpdate(sql, beanSources);
    }

    public HashMap export(Integer id) {
        ValueOperations<Serializable, Serializable> operations = redisTemplate.opsForValue();
        return (HashMap<Integer, Record>) operations.get("repoPlanList" + id);
    }

    public List<Repo> findRepos(Plan plan) {
        String name = plan.getName();
        String srcRepo = plan.getSrcRepo();
        ArrayList<Repo> repoList = new ArrayList<>();
        if (name != null && name.length() > 0) {
            Repo repo = repoMapper.findByName(name);
            if (repo != null) {
                repoList.add(repo);
            }
        } else if (srcRepo != null && srcRepo.length() > 0) {
            repoList = (ArrayList<Repo>) repoMapper.findBySrcRepo(srcRepo);
        } else {
            Integer start = plan.getStart();
            Integer end = plan.getEnd();
            if (start > 1) {
                if (end < start) end = start;
                repoList = (ArrayList<Repo>) findReposBetweenStartAndEnd(start, end);
            }
        }
        if (repoList != null && repoList.size() > 0) {
            try {
                ValueOperations<Serializable, Serializable> operations = redisTemplate.opsForValue();
                operations.set("repoPlanList" + plan.getId(), repoList, 30 * 60, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return repoList;
    }

    public void delete(Integer id) {
        planMapper.deleteById(id);
    }

    public void edit(Plan plan) {
        String datetime = getDatetime();
        plan.setTime(datetime);
        planMapper.updateById(plan);
    }

//    @Autowired
//    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
//
//    public <T> void saveMsgItemList(List<T> list) {
//        // 这里注意VALUES要用实体的变量，而不是字段的Column值
//        String sql = "INSERT INTO t_repo(id, name,version,srcRepo,srcCommit," +
//                "srcPath,requestedAt,comments,repoKeyPresent,existInPre,prUrl )" +
//                " values(:id, :name,:version,:srcRepo,:srcCommit," +
//                ":srcPath,:requestedAt,:comments,:repoKeyPresent,:existInPre,:prUrl )";
//        updateBatchCore(sql, list);
//    }
//
//    /**
//     * 一定要在jdbc url 加&rewriteBatchedStatements=true才能生效
//     * @param sql  自定义sql语句，类似于 "INSERT INTO chen_user(name,age) VALUES (:name,:age)"
//     * @param list
//     * @param <T>
//     */
//    public <T> void updateBatchCore(String sql, List<T> list) {
//        SqlParameterSource[] beanSources = SqlParameterSourceUtils.createBatch(list.toArray());
//        namedParameterJdbcTemplate.batchUpdate(sql, beanSources);
//    }

}
