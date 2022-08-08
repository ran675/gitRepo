package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.itheima.pojo.Permission;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface PermissionMapper extends Mapper<Permission> {
    @Select(" select * from t_permission where id in ( select permission_id from t_role_permission where role_id  = #{ roleId } )")
    Set<Permission> findByRoleId(Integer roleId);
}
