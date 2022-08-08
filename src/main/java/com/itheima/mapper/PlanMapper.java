package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Plan;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author v-rr
 */
public interface PlanMapper extends BaseMapper<Plan> {

    @Select("select * from t_plan where id = #{id}")
    Plan findById(Integer id);


//    @Select("<script>select * from t_plan " +
//            "<if test = 'queryString!=null and queryString.length > 0' >" +
//            "where srcRepo = #{ queryString} or name =#{queryString} </if> </script>")
//    Page<Plan> findByQueryString(String queryString);


//    @Insert("insert into t_plan(name,srcRepo,remark,start,end,executed) " +
//            "values( #{ name},#{srcRepo },#{remark },#{ start},#{end },#{ executed })")
//    void save(Plan plan);


    @Update("update t_plan set executed = #{value} where id = #{id}")
    void updateExecuted(Integer id, String value);


//    @Select("select * from t_record where planId = #{planId} and url not null")
//    Record findValidPlanId(Integer planId);

}
