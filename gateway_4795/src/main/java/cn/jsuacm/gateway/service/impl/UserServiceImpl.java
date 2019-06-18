package cn.jsuacm.gateway.service.impl;

import cn.jsuacm.gateway.mapper.UserMapper;
import cn.jsuacm.gateway.pojo.User;
import cn.jsuacm.gateway.pojo.enity.MessageResult;
import cn.jsuacm.gateway.service.UserService;
import cn.jsuacm.gateway.util.EmailUtil;
import cn.jsuacm.gateway.util.MD5Util;
import cn.jsuacm.gateway.util.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description 用户服务层实现
 * @Author h4795
 * @Date 2019/06/17 16:30
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private EmailUtil emailUtil;


    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @return
     */
    @Override
    public MessageResult sendRegisterEmail(String email) {
        if (checkEmail(email)){
            // 如果有这个在一分钟之内发送过信息
            if (redisUtil.hasKey(email)){
                return new MessageResult(false, String.valueOf(redisUtil.getExpire(email)));
            }
            if (redisUtil.hasKey("reg_"+email)){
                redisUtil.del("reg_"+email);
            }
            // 生成随机码
            String random = RandomStringUtils.random(6, false, true);
            MessageResult messageResult = emailUtil.sendBindingMail(email, random);
            redisUtil.set("reg_"+email, random, EmailUtil.EMAIL_TIME);
            redisUtil.set(email, 1, EmailUtil.SIGN_TIME);
            return messageResult;
        }
        return new MessageResult(false, "邮箱已经被注册");
    }

    @Override
    public MessageResult registerUser(User user, String code) {
        if(!checkEmail(user.getEmail())){
            return new MessageResult(false,"邮箱已经被注册");
        }
        if (!checkAccountNUmber(user.getAccountNumbser())){
            return new MessageResult(false, "用户名已经被注册");
        }
        if (checkUser(user) && checkEmailCode("reg_"+user.getEmail(), code)) {
            // 密码转换md5后再次加密
            user.setPassword(MD5Util.convertMD5(MD5Util.string2MD5(user.getPassword())));
            userMapper.insert(user);
            return new MessageResult(true, "注册成功");
        }
        return new MessageResult(false, "注册信息或者验证码未正确填写");
    }


    /**
     * 检查
     * @param email 邮箱账户
     * @param code 验证码
     * @return
     */
    @Override
    public boolean checkEmailCode(String email, String code) {
        if (redisUtil.hasKey(email)){
            String acode = String.valueOf(redisUtil.get(email));
            if (acode.equals(code)){
                redisUtil.del(email);
                return true;
            }
        }
        return false;
    }

    /**
     * 发送信息修改邮件
     *
     * @param email
     * @return
     */
    @Override
    public MessageResult sendUpdateEmail(String email) {


        if (!checkEmail(email)){
            // 如果有这个在一分钟之内发送过信息
            if (redisUtil.hasKey(email)){
                return new MessageResult(false, String.valueOf(redisUtil.getExpire(email)));
            }
            if (redisUtil.hasKey("upd_"+email)){
                redisUtil.del("upd_"+email);
            }
            // 生成随机码
            String random = RandomStringUtils.random(6, false, true);
            MessageResult messageResult = emailUtil.sendUpdataEmail(email, random);
            redisUtil.set("upd_"+email, random, EmailUtil.EMAIL_TIME);
            redisUtil.set(email, 1, EmailUtil.SIGN_TIME);
            return messageResult;
        }
        return new MessageResult(false, "没有这个邮箱");
    }

    /**
     * 通过验证邮箱更改密码
     *
     * @param email    用户的邮箱
     * @param newPwd 新的密码
     * @param code 验证码
     * @return
     */
    @Override
    public MessageResult updatePasswordByEmail(String email, String newPwd, String code) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email",email);
        List<User> users = userMapper.selectList(wrapper);

        if (users.size() == 0){
            return new MessageResult(false, "该邮箱没有注册");
        }

        User user = users.get(0);
        if (checkEmailCode("upd_"+user.getEmail(),code)) {
            MessageResult messageResult = updatePwd(user, newPwd);
            return messageResult;
        }else {
            return new MessageResult(false,"验证码错误");
        }

    }

    /**
     * 通过旧密码修改密码
     *
     * @param uid    用户的id
     * @param oldPwd 旧的密码
     * @param newPwd 新的密码
     * @return
     */
    @Override
    public MessageResult updatePassword(int uid, String oldPwd, String newPwd) {
        User user = userMapper.selectById(uid);
        if (MD5Util.convertMD5(user.getPassword()).equals(MD5Util.string2MD5(oldPwd))){
            MessageResult messageResult = updatePwd(user, newPwd);
            return messageResult;

        }else {
            return new MessageResult(false, "原密码错误");
        }
    }

    /**
     * 修改用户的用户名
     *
     * @param uid  用户的id
     * @param name 新的用户名
     * @return
     */
    @Override
    public MessageResult updateUsername(int uid, String name) {
        User user = userMapper.selectById(uid);
        user.setUsername(name);
        userMapper.updateById(user);
        return new MessageResult(true, "修改成功");
    }

    /**
     * 修改用户的头像链接
     *
     * @param uid 用户的id
     * @param url 用户的头像链接
     * @return
     */
    @Override
    public MessageResult updatePicUrl(int uid, String url) {
        User user = userMapper.selectById(uid);
        user.setPrictureUrl(url);
        userMapper.updateById(user);
        return new MessageResult(true, "修改成功");
    }

    /**
     * 修改用户的邮箱
     *
     * @param uid   用户的id
     * @param email 用户的邮箱
     * @return
     */
    @Override
    public MessageResult updateEmail(int uid, String email, String code) {
        User user = userMapper.selectById(uid);
        if (checkEmailCode("upd_"+user.getEmail(),code)) {
            user.setEmail(email);
            userMapper.updateById(user);
            return new MessageResult(true, "修改成功");
        }else {
            return new MessageResult(false, "验证码错误");
        }
    }

    /**
     * 通过账号获取用户信息
     *
     * @param accountNumber
     * @return
     */
    @Override
    public User getUser(String accountNumber) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account_number", accountNumber);
        List<User> users = userMapper.selectList(wrapper);
        if (users == null){
            return null;
        }
        return users.get(0);
    }

    /**
     * 管理员直接修改密码
     *
     * @param uid    用户id
     * @param newPwd 新的密码
     * @return
     */
    @Override
    public MessageResult updatePassword(int uid, String newPwd) {
        User user = userMapper.selectById(uid);
        MessageResult messageResult = updatePwd(user, newPwd);
        return messageResult;
    }

    /**
     * 查询邮箱是否注册过
     * @param email 邮箱
     * @return
     */
    private boolean checkEmail(String email){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email",email);
        Integer count = userMapper.selectCount(wrapper);
        if (count == 0){
            return true;
        }else {
            return false;
        }
    }


    /**
     * 检查用户账户是否已经被注册
     * @param accountNumber
     * @return
     */
    private boolean checkAccountNUmber(String accountNumber){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account_number", accountNumber);
        Integer count = userMapper.selectCount(wrapper);
        if (count == 0){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 检查用户信息是否正确
     * @param user
     * @return
     */
    private boolean checkUser(User user){
        // 如果用户名为空
        if (user.getAccountNumbser() == null || "".equals(user.getAccountNumbser())){
            return false;
        }
        if (user.getEmail() == null || "".equals(user.getEmail())){
            return false;
        }
        if (user.getPassword() == null || "".equals(user.getPassword())){
            return false;
        }
        if (user.getUsername() == null || "".equals(user.getUsername())){
            return false;
        }
        return true;
    }

    /**
     * 修改密码
     * @param user
     * @param newPwd
     * @return
     */
    @Transactional
    MessageResult updatePwd(User user, String newPwd){
        try {
            String decodePwd = MD5Util.convertMD5(MD5Util.string2MD5(newPwd));
            user.setPassword(decodePwd);
            userMapper.updateById(user);
            user = userMapper.selectById(user.getUid());
            if (!MD5Util.string2MD5(newPwd).equals(MD5Util.convertMD5(user.getPassword()))) {
                throw new Exception("密码修改失败");
            } else {
                return new MessageResult(true, "修改成功");
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new MessageResult(false, e.getMessage());
        }
    }
}
