package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Record;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface RecordMapper extends BaseMapper<Record> {

    @Select("select * from t_record where repoId = #{repoId}")
    Set<Record> findByRepoId(Integer repoId);

    @Select("select * from t_record where planId = #{planId}")
    Set<Record> findByPlanId(Integer planId);

    int insertSelective(Record record);


//    @Select("select * from t_record where repoId = #{repoId} and url not null")
//    Record findValidRepoId(Integer repoId);

}
