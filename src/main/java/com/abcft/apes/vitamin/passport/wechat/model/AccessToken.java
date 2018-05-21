/**
 * 版本信息：
 * 日期：2016年5月31日
 * Copyright 星河驾保 Corporation 2016
 * 版权所有
 */
package com.abcft.apes.vitamin.passport.wechat.model;

import com.alibaba.fastjson.JSON;

/**
 * 微信网页授权获取access_token返回对象模型
 * 
 * 创建人：陈军营
 * 创建时间：2016年5月31日 下午3:55:34
 */
public class AccessToken {

    // 网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
    private String access_token;
    // access_token接口调用凭证超时时间，单位（秒）
    private Integer expires_in;
    // 用户刷新access_token
    private String refresh_token;
    // 用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
    private String openid;
    // 用户授权的作用域，使用逗号（,）分隔
    private String scope;

    public AccessToken() {
    };

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Integer getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(Integer expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
