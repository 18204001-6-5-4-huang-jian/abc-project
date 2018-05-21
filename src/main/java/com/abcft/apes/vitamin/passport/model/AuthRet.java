package com.abcft.apes.vitamin.passport.model;

import com.alibaba.fastjson.JSON;

public class AuthRet {
	// 认证状态，0，未认证；1，认证中；2，已认证
	private int authentication;
	
	public AuthRet() {
	};
	
	public AuthRet(int authentication) {
		this.authentication = authentication;
	};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public int getAuthentication() {
		return authentication;
	}
	
	public void setAuthentication(int authentication) {
		this.authentication = authentication;
	}
}
