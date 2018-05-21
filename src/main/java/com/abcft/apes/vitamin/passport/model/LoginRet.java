package com.abcft.apes.vitamin.passport.model;

import com.alibaba.fastjson.JSON;

public class LoginRet {
	// token
	private TokenRet token;
	
	// 用户信息
	private UserInfoRet user;
	
	public LoginRet() {
	};
	
	public LoginRet(TokenRet token, UserInfoRet user) {
		this.token = token;
		this.user = user;
	};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public TokenRet getToken() {
		return token;
	}
	
	public void setToken(TokenRet token) {
		this.token = token;
	}
	
	public UserInfoRet getUser() {
		return user;
	}
	
	public void setUser(UserInfoRet user) {
		this.user = user;
	}
	
}
