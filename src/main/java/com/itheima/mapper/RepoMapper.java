package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Repo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RepoMapper extends BaseMapper<Repo> {

//    @Select("<script>select * from t_repo " +
//            "<if test = 'queryString!=null and queryString.length > 0' >where id = #{ queryString} or name =#{queryString} </if> </script>")
//    Page<Repo> findByQueryString(@Param("queryString") String queryString);

//    @Select("select * from t_repo where id = #{id} ")
//    Repo findById(Integer id);


    @Select("insert into t_repo values(#{id},#{name},#{version},#{srcRepo},#{srcCommit},  #{srcPath},#{requestedAt},#{comments},#{repoKeyPresent},#{existInPre},#{prUrl})")
    void save1Repo(Repo fs);

//    @Select("select * from t_repo where id between #{start} and #{end} ")
//    @MapKey("id")
//    HashMap<Integer, Repo> findReposBetweenStartAndEnd(@Param("start")Integer start, @Param("end")Integer end);


    @Select("select * from t_repo where name = #{name} ")
    Repo findByName(String name);


    @Select("select * from t_repo where srcRepo = #{srcRepo} ")
    @MapKey("id")
    List<Repo> findBySrcRepo(String srcRepo);


}
