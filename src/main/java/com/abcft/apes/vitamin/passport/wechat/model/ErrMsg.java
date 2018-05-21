/**
 * 版本信息：
 * 日期：2016年5月31日
 * Copyright 星河驾保 Corporation 2016
 * 版权所有
 */
package com.abcft.apes.vitamin.passport.wechat.model;

import com.alibaba.fastjson.JSON;

/**
 * 微信调用出错，信息返回
 * 
 * 创建人：陈军营
 * 创建时间：2016年5月31日 下午4:09:56
 */
public class ErrMsg {

    // 错误码
    private Integer errcode;
    // 错误信息
    private String errmsg;

    public ErrMsg() {
    };

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public Integer getErrcode() {
        return errcode;
    }

    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

}
