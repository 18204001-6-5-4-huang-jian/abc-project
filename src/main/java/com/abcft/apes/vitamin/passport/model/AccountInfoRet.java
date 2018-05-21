package com.abcft.apes.vitamin.passport.model;

import com.alibaba.fastjson.JSON;

public class AccountInfoRet {
	// 用户ID
	private String id;
	
	// 用户名称
	private String username;
	
	// 邮箱
	private String email;
	
	// 用户角色
	private String role;
	
	// 昵称
	private String nickname;
	
	// 头像
	private String head_img;
	
	// 认证状态，0，未认证；1，认证中；2，已认证；3，未认证微信
	private int authentication;
	
	// 真实姓名
	private String real_name;
	
	// 手机号
	private String mobile;
	
	// 职位
	private String profession;
	
	// 机构名称
	private String organization;
	
	public AccountInfoRet() {
	};
	
	public AccountInfoRet(String id, String username, String email,
			String role, String nickname, String head_img, int authentication,
			String real_name, String mobile, String profession, String organization) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.role = role;
		this.nickname = nickname;
		this.head_img = head_img;
		this.authentication = authentication;
		this.real_name = real_name;
		this.mobile = mobile;
		this.profession = profession;
		this.organization = organization;
	};
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getHead_img() {
		return head_img;
	}
	
	public void setHead_img(String head_img) {
		this.head_img = head_img;
	}
	
	public int getAuthentication() {
		return authentication;
	}
	
	public void setAuthentication(int authentication) {
		this.authentication = authentication;
	}
	
	public String getReal_name() {
		return real_name;
	}
	
	public void setReal_name(String real_name) {
		this.real_name = real_name;
	}
	
	public String getMobile() {
		return mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String getProfession() {
		return profession;
	}
	
	public void setProfession(String profession) {
		this.profession = profession;
	}
	
	public String getOrganization() {
		return organization;
	}
	
	public void setOrganization(String organization) {
		this.organization = organization;
	}
}
