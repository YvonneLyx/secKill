package com.miaosha.controller;

import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public final static String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception，都抛给了tomcat
    //业务处理最后一道关口，Spring钩子处理思想
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody //不加这个显示的还是白页 error page
//    public CommonReturnType handlerException(HttpServletRequest request, Exception ex) {
//        Map<String, Object> respData = new HashMap<>();
//        if(ex instanceof BusinessException) {
//            BusinessException be = (BusinessException)ex;
//
//            respData.put("errorCode", be.getErrCode());
//            respData.put("errorMsg", be.getErrMsg());
//        }else {
//            respData.put("errorCode", EmBusinessError.UNKOWN_ERROR.getErrCode());
//            respData.put("errorMsg", EmBusinessError.UNKOWN_ERROR.getErrMsg());
//        }
//
//        return CommonReturnType.create(respData, "fail");

//        CommonReturnType type = new CommonReturnType();
//        type.setStatus("fail");
//        type.setData(ex);
//        return type;
//        return null;
//    }
}
