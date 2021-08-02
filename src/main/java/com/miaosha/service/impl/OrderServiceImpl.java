package com.miaosha.service.impl;

import com.miaosha.dao.*;
import com.miaosha.dataobject.ItemDO;
import com.miaosha.dataobject.OrderDO;
import com.miaosha.dataobject.SequenceDO;
import com.miaosha.dataobject.UserDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.ItemService;
import com.miaosha.service.OrderService;
import com.miaosha.service.PromoService;
import com.miaosha.service.UserService;
import com.miaosha.service.model.ItemModel;
import com.miaosha.service.model.OrderModel;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

//    @Autowired
//    private PromoService promoService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Transactional
    @Override
    public OrderModel createModel(Integer userId, Integer itemId, Integer amount, Integer promoId) throws BusinessException {
        //校验用户是否存在， 商品是否存在，购买数量是否正确
//        ItemModel itemModel = itemService.getItemDetail(itemId);
//        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null) throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL, "商品信息不存在");

//        UserModel user = userService.getUser(userId);
//        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        UserModel user = userService.getUserByIdInCache(userId);
        if(user == null) throw new BusinessException(EmBusinessError.USER_NOT_EXIST);

        if(amount < 0 || amount >= 99) throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "下单数量不正确");

        //为null 是普通商品
        if(promoId != null) {
            //校验对应活动是否存在这个适用商品
            if(promoId != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品活动不匹配");
            }else{
                //还要校验活动是不是在进行中
                if(itemModel.getPromoModel().getStatus() != 2) {
                    throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
                }
            }
        }

        //落单减库存（采取这种）
        //支付减库存
        boolean b = itemService.decreaseStock(itemId, amount);
        if(!b) throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);

        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);

        if(promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(new BigDecimal(amount).multiply(orderModel.getItemPrice()));
        orderModel.setPromoId(promoId);

        //还要生成主键id，订单号
        String sid = generateOrderNo();
        orderModel.setId(sid);

        OrderDO orderDO = convertFromModel(orderModel);

        orderDOMapper.insertSelective(orderDO);

        //增加销量
        itemService.increaseSale(itemId, amount);

        //返回给前端
        return orderModel;
    }

    public OrderDO convertFromModel(OrderModel orderModel) {
        if(orderModel == null) return null;
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW) //作用： 自增序列作为全局变量，就算没有insert成功也要生成
    public String generateOrderNo() {
        /**
         * 订单号16位
         * 前8位年月日
         * 中间6位自增序列
         * 后2位分库分表位
         */
        StringBuilder sb = new StringBuilder();
        LocalDateTime time = LocalDateTime.now();
        String s = time.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        sb.append(s);

//        SequenceDO sequenceDO = sequenceDOMapper.selectByPrimaryKey("order_info");
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        int sequence = sequenceDO.getCurrentValue();
//        sequence += sequenceDO.getStep();
        sequenceDO.setCurrentValue(sequence + sequenceDO.getStep());
//        System.out.println(sequenceDO.getStep() +" "+sequenceDO.getName()+" "+sequenceDO.getCurrentValue());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

//        Integer value = sequenceDOMapper.getSequenceByName("order_info").getCurrentValue();
//        System.out.println(value);
        String sequenceStr = String.valueOf(sequence);

        //不足6位的要补全，先不考虑 超过6位的情况了。。
        for(int i = 0; i < 6 - sequenceStr.length(); i++) {
            sb.append("0");
        }
        sb.append(sequence);

        //后两位先写死
        sb.append("00");

        return sb.toString();


    }

}
