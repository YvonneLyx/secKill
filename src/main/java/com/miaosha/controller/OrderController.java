package com.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.OrderService;
import com.miaosha.service.model.OrderModel;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin( allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType create(
//            @RequestParam(name = "user_id") Integer userId,
                                   @RequestParam(name = "itemId") Integer itemId,
                                   @RequestParam(name = "amount") Integer amount,
                                   @RequestParam(name = "promoId", required = false) Integer promoId)  throws BusinessException {

//        Boolean login = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(login == null || !login.booleanValue()) {
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
//        }
        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if(token == null) {
        if(StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }

        UserModel user = (UserModel) redisTemplate.opsForValue().get(token);
        if(user == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }



//        UserModel user = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel model = orderService.createModel(user.getId(), itemId, amount, promoId);
//        OrderVO orderVO = convertFromModel(model);
//        return CommonReturnType.create(orderVO);
        return CommonReturnType.create(null);
    }

//    public OrderVO convertFromModel(OrderModel orderModel) {
//        if(orderModel == null) return null;
//        OrderVO orderVO = new OrderVO();
//        BeanUtils.copyProperties(orderModel, orderVO);
//        return orderVO;
//    }
}
