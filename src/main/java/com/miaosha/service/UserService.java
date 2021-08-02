package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.model.UserModel;

public interface UserService {

    UserModel getUser(int id);

    void register(UserModel userModel) throws BusinessException;

    UserModel login(String telphone, String paramPswd) throws BusinessException;

    UserModel getUserByIdInCache(Integer userId);
}
