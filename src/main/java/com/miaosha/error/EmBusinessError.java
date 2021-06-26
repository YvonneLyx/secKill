package com.miaosha.error;


//把错误信息取出来
//枚举本质面向对象的类
public enum EmBusinessError implements CommonError{

    //通用错误类型00001，传过来的各种参数不对，邮箱，姓名
    PARAMETER_VALIDATION_ERROR(100001, "参数不合法"),
    UNKOWN_ERROR(100002, "未知错误"),

    //200000开头为用户信息相关错误定义
    USER_NOT_EXIST(200001, "用户不存在"),
    USER_LOGIN_FAIL(200002, "用户名或密码错误"),
    USER_NOT_LOGIN(200003, "用户名或密码错误"),

    //300000开头为交易信息错误定义
    STOCK_NOT_ENOUGH(300001, "库存不足")

    ;

    private int errCode;
    private String errMsg;

    EmBusinessError(int code, String errMsg) {
        this.errCode = code;
        this.errMsg = errMsg;
    }


    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    //通用错误类型
    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
