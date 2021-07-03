package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.OrderModel;

public interface OrderService {

    //通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品并且活动已开始

    //直接在下单接口内通过商品id查看是否存在秒杀活动，若存在则以秒杀价格下单
    // （不采取这种，
    // 1.一个商品可能存在多种秒杀活动 2. 下单接口再查活动信息对于性能不好
    OrderModel createModel(Integer userId, Integer itemId, Integer amount, Integer promoId) throws BusinessException;
}
