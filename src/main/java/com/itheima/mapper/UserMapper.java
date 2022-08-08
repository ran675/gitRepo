package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.itheima.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends Mapper<User> {

    @Select("select * from t_user where username = #{username}")
    User findByUsername(String username);

    @Select("select * from t_user where id = #{id}")
    User findById(Long id);

    @Insert("insert into t_user (birthday, gender, username, password, remark, station, telephone) " +
            "VALUES ( #{birthday}, #{gender}, #{username}, #{password}, #{remark}, #{station}, #{telephone} )")
    void saveUser(User user);


}
