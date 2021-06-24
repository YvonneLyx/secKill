package com.miaosha.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationResult {
    //校验结果是否有错
    private boolean hasErrors = false;

    //存放错误信息
    Map<String, String> errMsgMap = new HashMap<>();

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public Map<String, String> getErrMsgMap() {
        return errMsgMap;
    }

    public void setErrMsgMap(Map<String, String> errMsgMap) {
        this.errMsgMap = errMsgMap;
    }

    //实现通用的通用格式化字符串信息获取
    public String getErrMsg(){
        String str = StringUtils.join(errMsgMap.values().toArray(new String[0]), ",");
        return str;
    }
}
