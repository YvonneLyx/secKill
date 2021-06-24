package com.miaosha.response;


public class CommonReturnType {
    //返回给前端的格式status+data
    private String status; //表明是fail还是success


    //若status=success，则返回的是前端需要的json
    //若status=fail，则data内使用通用的错误码形式
    private Object data;


    public static CommonReturnType create(Object result) {
        return create(result, "success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setStatus(status);
        commonReturnType.setData(result);
        return commonReturnType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
