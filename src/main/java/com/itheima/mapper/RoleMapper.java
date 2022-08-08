package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Role;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface RoleMapper extends BaseMapper<Role> {

    @Select(" select * from t_role where id in ( select role_id from t_user_role where user_id  = #{ userId } )")
    Set<Role> findByUserId(Integer userId);

}
