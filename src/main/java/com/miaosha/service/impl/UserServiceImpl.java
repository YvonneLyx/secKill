package com.miaosha.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.dao.UserDOMapper;
import com.miaosha.dao.UserPasswordDOMapper;
import com.miaosha.dataobject.UserDO;
import com.miaosha.dataobject.UserPasswordDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.UserService;
import com.miaosha.service.model.UserModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    //通过id获取对象
    @Override
    public UserModel getUser(int id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null) return null;

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);
        return convertFromDataObject(userDO, userPasswordDO);
    }

    @Override
    @Transactional //两个入库
    //void 不是CommonReturnType！！！
    public void register(UserModel userModel) throws BusinessException {
        System.out.println("aaa");
        if(userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //校验没加！！！
//        if(StringUtils.isEmpty(userModel.getTelphone()) || userModel.getAge() == null
//            || userModel.getGender() == null || StringUtils.isEmpty(userModel.getName())){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }

        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }


        UserDO userdo = ConvertFromUserModel(userModel);
//        System.out.println("1::"+userdo.getId());
        userDOMapper.insertSelective(userdo);

//        System.out.println("2:::"+userdo.getId());
        //注册到userDo之后再获取到对应id
        userModel.setId(userdo.getId());

        UserPasswordDO upd = ConvertPasswordFromUserModel(userModel);
//        System.out.println(upd);
        try {
            userPasswordDOMapper.insertSelective(upd);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号重复");
        }

        System.out.println("wtf");
        return;
    }

    @Override
    public UserModel login(String telphone, String paramPswd) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO == null) throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        if(!StringUtils.equals(userPasswordDO.getEncrptPassword(), paramPswd)) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserModel userModel = new UserModel();
        UserModel model = convertFromDataObject(userDO, userPasswordDO);
        return model;

    }

    @Override
    public UserModel getUserByIdInCache(Integer userId) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_"+ userId);

        if(userModel == null) {
            userModel = this.getUser(userId);
            redisTemplate.opsForValue().set("user_validate_" + userId, userModel);
        }
        return userModel;
    }

    private UserDO ConvertFromUserModel(UserModel userModel) throws BusinessException {
//        if(userModel == null) throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        if(userModel == null) return null;
        UserDO userDo = new UserDO();
        BeanUtils.copyProperties(userModel, userDo);
        return userDo;
    }

    private UserPasswordDO ConvertPasswordFromUserModel(UserModel userModel) throws BusinessException {
//        if(userModel == null) throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        if(userModel == null) return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
//        userPasswordDO.setId(userModel.getId());
        return userPasswordDO;

    }


    public UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if(userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();

        BeanUtils.copyProperties(userDO, userModel); //将userDO 拷贝到 userModel里
        if(userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword()); //不能直接copy，因为两个里面都有id
        }

        return userModel;


    }
}
