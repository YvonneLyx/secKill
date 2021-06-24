package com.miaosha.controller;


import com.alibaba.druid.util.StringUtils;
import com.miaosha.controller.Mode.UserVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.UserService;
import com.miaosha.service.impl.UserServiceImpl;
import com.miaosha.service.model.UserModel;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;
import sun.security.provider.MD5;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping(value = "/user")
@CrossOrigin(originPatterns = "*", allowCredentials = "true", allowedHeaders = "*") //解决跨域问题 本地访问localhost 存在跨域问题  不能session共享
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    //bean 单例模式，一个requset支持多个用户并发访问吗？？？
    //springbean包装的servlet，本质是一个proxy，内部是ThreadLocalMap，用户可以在每个线程中处理自己的request，threadLocal清除机制。
    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if(StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password) ) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        UserModel userModel = userService.login(telphone, this.encodeByMd5(password));
//
//        if(userModel == null) {
//            return CommonReturnType.create("用户名密码错误");
//        }

        //将登录凭证记录到session内
        this.request.getSession().setAttribute("IS_LOGIN", true);
        this.request.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }


    //用户注册
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "otpCode" ) String otp,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otp一致
        String sessionOtp = (String) this.request.getSession().getAttribute(telphone);
//        if(!StringUtils.equals(sessionOtp, otp)){
//            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
//        }

        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
//        userModel.setGender(gender);//强转
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone"); //usermodel里面的字段 不能看userVO
//        userModel.setEncrptPassword(MD5Encoder.encode(password.getBytes())); //要用md5加密,自带只能加密16位
        userModel.setEncrptPassword(encodeByMd5(password));

        System.out.println("a1");

//        UserService userService = new UserServiceImpl();
        userService.register(userModel);

        //没注册啊 ！得调用userservice

        return CommonReturnType.create(null);


    }

    public String encodeByMd5(String pswd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        //加密字符串
        String s = base64Encoder.encode(md5.digest(pswd.getBytes("utf-8")));
        return s;
    }


    @RequestMapping(value="/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
//    @CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
    public CommonReturnType getOpt(@RequestParam(name = "telphone") String telphone) {
        //1.随机生成验证码
        Random random = new Random();
        int nextInt = random.nextInt(99999);//[0,99999)
        nextInt += 10000;//[10000, 109999)
        //返回的是字符串
        String otpCode = String.valueOf(nextInt);


        //2.将用户的telphone和验证码绑定，目前存session里，后续要优化，存到redis里，因为redis本身就是键值对，适合存储这种，还可以设定expireTime，以及不断覆盖otp
        //目前使用httpsession的方式绑定手机号和otp

        request.getSession().setAttribute(telphone, otpCode);

        //3.返回给用户otp
        //企业级要打log，不能直接把用户信息暴露出来
        System.out.println(telphone+" " +otpCode);

        return CommonReturnType.create(null);
    }



    //根据id查询用户
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        UserModel userModel = userService.getUser(id);
//        return userModel;

        if(userModel == null) {
//            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            userModel.getAge();
        }

        //将userModel转化给前端ui所需
        UserVO userVO = converFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    public UserVO converFromModel(UserModel userModel) {
        if(userModel == null) return null;
        UserVO userVO = new UserVO();
//        int i = 1 / 0;
        BeanUtils.copyProperties(userModel, userVO);

        return userVO;
    }



}
