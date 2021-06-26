package com.miaosha.controller;

import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.OrderService;
import com.miaosha.service.model.OrderModel;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType create(
//            @RequestParam(name = "user_id") Integer userId,
                                   @RequestParam(name = "item_id") Integer itemId,
                                   @RequestParam(name = "amount") Integer amount) throws BusinessException {

//        Boolean login = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
//        if(login == null || !login.booleanValue()) {
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
//        }
//        UserModel user = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel model = orderService.createModel(Integer.valueOf(24), itemId, amount);
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
