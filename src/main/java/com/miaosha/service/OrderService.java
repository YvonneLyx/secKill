package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.OrderModel;

public interface OrderService {

    OrderModel createModel(Integer userId, Integer itemId, Integer amount) throws BusinessException;
}
