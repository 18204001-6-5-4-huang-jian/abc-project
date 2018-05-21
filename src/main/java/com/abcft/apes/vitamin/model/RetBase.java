package com.abcft.apes.vitamin.model;

import com.alibaba.fastjson.JSON;

public class RetBase {
	protected boolean success;
	protected int status;
	protected String message;
	
	public RetBase() {
	};
	
	public RetBase(boolean success, int status, String message) {
		this.success = success;
		this.status = status;
		this.message = message;
	};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
}
