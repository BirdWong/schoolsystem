package cn.jsuacm.gateway.service.impl;

import cn.jsuacm.gateway.mapper.AuthenticationMapper;
import cn.jsuacm.gateway.mapper.UserMapper;
import cn.jsuacm.gateway.pojo.Authentication;
import cn.jsuacm.gateway.pojo.User;
import cn.jsuacm.gateway.service.AuthenticationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AuthenticationServiceImpl
 * @Description 权限服务实现
 * @Author h4795
 * @Date 2019/06/17 21:45
 */
@Service
public class AuthenticationServiceImpl extends ServiceImpl<AuthenticationMapper, Authentication> implements AuthenticationService{

    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private UserMapper userMapper;


    /**
     * 添加用户实验室成员权限
     *
     * @param uid 被添加权限的id
     * @return
     */
    @Override
    public boolean addMemBerAuthentication(int uid) {
        return addRole(uid, AuthenticationService.MENBER);
    }

    /**
     * 添加管理员权限
     *
     * @param uid 被添加权限的id
     * @return
     */
    @Override
    public boolean addAdminAuthentication(int uid) {
        return addRole(uid, AuthenticationService.ADMIN);
    }

    /**
     * 添加老师权限
     *
     * @param uid 被添加权限的id
     * @return
     */
    @Override
    public boolean addTeacherAuthentication(int uid) {
        return addRole(uid, AuthenticationService.TEACHER);
    }

    /**
     * 添加超级管理员权限
     *
     * @param uid 被添加权限的id
     * @return
     */
    @Override
    public boolean addAdministratorAuthentication(int uid) {
        return addRole(uid, AuthenticationService.ADMINISTRATOR);
    }

    /**
     * 删除用户实验室成员权限
     *
     * @param uid 被删除权限的id
     * @return
     */
    @Override
    public boolean deleteMemBerAuthentication(int uid) {
        return deleteRole(uid, AuthenticationService.MENBER);
    }

    /**
     * 删除管理员权限
     *
     * @param uid 被删除权限的id
     * @return
     */
    @Override
    public boolean deleteAdminAuthentication(int uid) {
        return deleteRole(uid, AuthenticationService.ADMIN);
    }

    /**
     * 删除老师权限
     *
     * @param uid 被删除权限的id
     * @return
     */
    @Override
    public boolean deleteTeacherAuthentication(int uid) {
        return deleteRole(uid, AuthenticationService.TEACHER);
    }

    /**
     * 删除超级管理员权限
     * @param uid 被删除权限的id
     * @return
     */
    @Override
    public boolean deleteAdministratorAuthentication(int uid) {
        return deleteRole(uid, AuthenticationService.ADMINISTRATOR);
    }

    /**
     * 通过用户账号获取用户的所有权限
     *
     * @param uid
     * @return
     */
    @Override
    public List<String> getGrantedAuthorities(int uid) {
        QueryWrapper<Authentication> wrapper = new QueryWrapper<>();
        wrapper.eq("uid", uid);
        List<Authentication> authentications = authenticationMapper.selectList(wrapper);
        List<String> grantedAuthorities = new ArrayList<>();
        for (Authentication authentication : authentications){
            grantedAuthorities.add(authentication.getRole());
        }

        return grantedAuthorities;
    }


    /**
     * 给指定用户添加权限
     * @param uid 用户的id
     * @param role 添加的权限
     * @return
     */
    private boolean addRole(int uid, String role){
        // 确认被添加的用户id是否存在
        User user = userMapper.selectById(uid);
        if (user == null){
            return false;
        }

        // 设置权限
        Authentication authentication = new Authentication();
        authentication.setUid(uid);
        authentication.setRole(role);
        // 确认是否已经拥有权限
        QueryWrapper<Authentication> wrapper = new QueryWrapper<>();
        wrapper.eq("uid",uid).eq("role",role);
        Integer count = authenticationMapper.selectCount(wrapper);
        // 如果没有这个权限或者出现多次
        if (count != 1){
            // 如果这个数据多次出现，先删除，后添加
            if (count > 1){
                authenticationMapper.delete(wrapper);
            }
            authenticationMapper.insert(authentication);

        }

        return true;
    }


    /**
     * 删除指定用户的相关权限
     * @param uid 用户的id
     * @param role 用户被删除的权限
     * @return
     */
    private boolean deleteRole(int uid, String role){
        //验证用户是否存在
        User user = userMapper.selectById(uid);
        if (user == null){
            return false;
        }

        // 设置删除的条件
        QueryWrapper<Authentication> wrapper = new QueryWrapper<>();
        wrapper.eq("uid",uid).eq("role", role);
        authenticationMapper.delete(wrapper);
        return true;
    }
}
