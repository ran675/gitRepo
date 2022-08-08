package com.itheima.service;

import com.itheima.mapper.PermissionMapper;
import com.itheima.mapper.RoleMapper;
import com.itheima.mapper.UserMapper;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    //根据用户名查询数据库获取用户信息和关联的角色信息，同时需要查询角色关联的权限信息

    public User findByUsername(String username) {
        //查询用户基本信息，不包含用户的角色
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return null;
        }
        Integer userId = user.getId();
        //根据用户ID查询对应的角色
        Set<Role> roles = roleMapper.findByUserId(userId);
        for (Role role : roles) {
            Integer roleId = role.getId();
            //根据角色ID查询关联的权限
            Set<Permission> permissions = permissionMapper.findByRoleId(roleId);
            role.setPermissions(permissions);
        }
        user.setRoles(roles);
        return user;
    }

    public User findById(Long id) {
        return userMapper.findById(id);
    }

    public void saveUser(User user) {
        User user1 = userMapper.findByUsername(user.getUsername());
        if (user1 == null) {
            userMapper.saveUser(user);
        } else {
            throw new RuntimeException("existing user, duplicate username");
        }
    }
}
