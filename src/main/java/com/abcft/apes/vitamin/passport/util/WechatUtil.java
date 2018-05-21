package com.abcft.apes.vitamin.passport.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.abcft.apes.vitamin.passport.wechat.model.AccessToken;
import com.abcft.apes.vitamin.passport.wechat.model.ErrMsg;
import com.abcft.apes.vitamin.passport.wechat.model.UserInfo;
import com.abcft.apes.vitamin.util.HttpUtil;
import com.alibaba.fastjson.JSON;

public class WechatUtil {
	private static Logger PASSPORT_LOG = Logger.getLogger("passport");
	
	// 微信商户信息
	public static String APPID;
	public static String SECRET;
	
	// 微信调用地址
	public static final String URL_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token";
	public static final String URL_REFRESH_TOKEN = "https://api.weixin.qq.com/sns/oauth2/refresh_token";
	public static final String URL_USER_INFO = "https://api.weixin.qq.com/sns/userinfo";
	public static final String URL_AUTH = "https://api.weixin.qq.com/sns/auth";
	public static final String URL_JSAPI_TICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
	public static final String URL_ACCESS_TOKEN_PUBLIC = "https://api.weixin.qq.com/cgi-bin/token";
	public static final String URL_PAY_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	public static final String URL_GET_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID";
	public static final String URL_PAY_REFUND = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	public static final String URL_PAY_COMPANY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	
	/**
	 * 用户同意授权，获取access_token
	 */
	public static AccessToken getAccessToken(String code) {
		AccessToken accessToken = null;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", APPID);
		params.put("secret", SECRET);
		params.put("code", code);
		params.put("grant_type", "authorization_code");
		
		String content = HttpUtil.doGet(URL_ACCESS_TOKEN, params);
		if (content != null) {
			accessToken = JSON.parseObject(content, AccessToken.class);
			
			String access_token = accessToken.getAccess_token();
			if (access_token == null) {
				ErrMsg errMsg = JSON.parseObject(content, ErrMsg.class);
				PASSPORT_LOG.info("errMsg=" + errMsg);
				
				accessToken = null;
			}
		}
		
		return accessToken;
	}
	
	/**
	 * 刷新access_token
	 */
	public static AccessToken refreshToken(String refresh_token) {
		AccessToken accessToken = null;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", APPID);
		params.put("grant_type", "refresh_token");
		params.put("refresh_token", refresh_token);
		
		String content = HttpUtil.doGet(URL_REFRESH_TOKEN, params);
		if (content != null) {
			accessToken = JSON.parseObject(content, AccessToken.class);
			
			String access_token = accessToken.getAccess_token();
			if (access_token == null) {
				ErrMsg errMsg = JSON.parseObject(content, ErrMsg.class);
				PASSPORT_LOG.info("errMsg=" + errMsg);
				
				accessToken = null;
			}
		}
		
		return accessToken;
	}
	
	/**
	 * 拉取用户信息
	 */
	public static UserInfo getUserInfo(String access_token, String openid) {
		UserInfo userInfo = null;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("access_token", access_token);
		params.put("openid", openid);
		params.put("lang", "zh_CN");
		
		String content = HttpUtil.doGet(URL_USER_INFO, params);
		if (content != null) {
			userInfo = JSON.parseObject(content, UserInfo.class);
			
			String nickname = userInfo.getNickname();
			if (nickname == null) {
				ErrMsg errMsg = JSON.parseObject(content, ErrMsg.class);
				PASSPORT_LOG.info("errMsg=" + errMsg);
				
				userInfo = null;
			}
		}
		
		return userInfo;
	}
	
	/**
	 * 检验授权凭证（access_token）是否有效，正确的JSON返回结果：{ "errcode":0,"errmsg":"ok"}
	 */
	public static boolean checkAccessToken(String access_token, String openid) {
		boolean check = false;
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("access_token", access_token);
		params.put("openid", openid);
		
		String content = HttpUtil.doGet(URL_AUTH, params);
		if (content != null) {
			ErrMsg errMsg = JSON.parseObject(content, ErrMsg.class);
			PASSPORT_LOG.info("errMsg=" + errMsg);
			
			Integer errcode = errMsg.getErrcode();
			if (errcode == 0) {
				check = true;
			}
		}
		
		return check;
	}
}
