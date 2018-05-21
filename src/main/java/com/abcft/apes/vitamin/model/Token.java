package com.abcft.apes.vitamin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class Token implements Serializable {
    private static final long serialVersionUID = -186954891131L;

    @JsonProperty("auth_token")
    private String authToken;

    @JsonProperty("expires")
    private Date expires;

    public Token() {}

    public String getAuthToken() { return authToken; }

    public void setAuthToken(String authToken) { this.authToken = authToken; }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS zzz", timezone = "Asia/Shanghai")
    public Date getExpires() { return this.expires; }

    public void setExpires(Date expires) { this.expires = expires; }
}
