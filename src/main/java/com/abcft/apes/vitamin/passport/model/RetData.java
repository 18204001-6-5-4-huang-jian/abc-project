package com.abcft.apes.vitamin.passport.model;

import com.abcft.apes.vitamin.model.RetBase;
import com.alibaba.fastjson.JSON;

public class RetData extends RetBase {
	private Object data;
	
	public RetData() {
	};
	
	public RetData(boolean success, int status, String message) {
		this.success = success;
		this.status = status;
		this.message = message;
	};
	
	public RetData(boolean success, int status, String message, Object data) {
		this.success = success;
		this.status = status;
		this.message = message;
		this.data = data;
	};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
