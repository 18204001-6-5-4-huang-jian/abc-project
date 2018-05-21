package com.abcft.apes.vitamin.passport.model;

import com.alibaba.fastjson.JSON;

public class TokenRet {
	private String auth_token;
	private String expiry;
	
	public TokenRet() {
	};
	
	public TokenRet(String auth_token, String expiry) {
		this.auth_token = auth_token;
		this.expiry = expiry;
	};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public String getAuth_token() {
		return auth_token;
	}
	
	public void setAuth_token(String auth_token) {
		this.auth_token = auth_token;
	}
	
	public String getExpiry() {
		return expiry;
	}
	
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
	
}
