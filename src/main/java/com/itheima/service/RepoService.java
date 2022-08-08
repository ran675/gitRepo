package com.itheima.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.mapper.RepoMapper;
import com.itheima.pojo.Repo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RepoService {

    @Autowired
    private RepoMapper repoMapper;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;//引入bean
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer pageSize = queryPageBean.getPageSize();
        Integer currentPage = queryPageBean.getCurrentPage();
        String queryString = queryPageBean.getQueryString();
        QueryWrapper<Repo> wrapper = new QueryWrapper<>();
        if (queryString != null) {
            wrapper.like("name", queryString).or().like("srcRepo", queryString);
        }
        Page<Repo> page = new Page<>(currentPage, pageSize, true);
        IPage<Repo> repoIPage = repoMapper.selectPage(page, wrapper);

        return new PageResult(repoIPage.getTotal(), repoIPage.getRecords());

    }


    public Repo findById(Integer id) {
        return repoMapper.selectById(id);
    }

//    @Autowired
//    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public <T> void saveMsgItemList(List<T> list) {
        // 这里注意VALUES要用实体的变量，而不是字段的Column值
        String sql = "INSERT INTO t_repo(id, name,version,srcRepo,srcCommit," +
                "srcPath,requestedAt,comments,repoKeyPresent,existInPre,prUrl )" +
                " values(:id, :name,:version,:srcRepo,:srcCommit," +
                ":srcPath,:requestedAt,:comments,:repoKeyPresent,:existInPre,:prUrl )";
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


//    public void batchInsert(List<Repo> RepoList){
//        SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);//关闭session的自动提交
//        RepoMapper mapper = session.getMapper(RepoMapper.class);//利用反射生成mapper对象
//        try {
//            int i=0;
//            for (Repo fs : RepoList) {
//                mapper.save1Repo(fs);
//                if (i % 1000 == 0 || i == RepoList.size()-1) {
//                    //手动每1000个一提交，提交后无法回滚
//                    session.commit();
//                    session.clearCache();//注意，如果没有这个动作，可能会导致内存崩溃。
//                }
//                i++;
//            }
//        }catch (Exception e) {
//            //没有提交的数据可以回滚
//            session.rollback();
//        } finally{
//            session.close();
//        }
//    }
}
