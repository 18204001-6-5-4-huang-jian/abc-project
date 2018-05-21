package com.abcft.apes.vitamin.util;

import com.abcft.apes.vitamin.model.Token;
import org.apache.log4j.Logger;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.bson.Document;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhyzhu on 17-4-23.
 */
public class TokenUtil {
    private static final Logger logger = Logger.getLogger(TokenUtil.class.getName());
    public static int TOKEN_EXPIRY_SECONDS = 2 * 24 * 60;

    public static Key getKey() {
        //String path=(context.getRealPath("/"));
        String path = FileUtil.getWorkingDirectory().getPath();
        File file=new File(path,"key.txt");
        try {
            if(!file.exists()){
                Key key = MacProvider.generateKey(SignatureAlgorithm.HS512);
                ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(file));
                oo.writeObject(key);
                oo.close();
                return key;
            } else {
                ObjectInputStream ois = null;
                ois = new ObjectInputStream(new FileInputStream(file));
                Key key= (Key) ois.readObject();
                return key;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            logger.debug(e.getMessage());
            return null;
        }
    }

    public static JsonObject generateToken(String userId){
        return generateToken(userId, 2 * 24 * 60);
    }

    public static JsonObject generateToken(String userId, int minutes){
        JsonObject userJson = AccountUtil.getUserById(userId);
        return generateToken(userJson, minutes);
    }

    public static JsonObject generateToken(JsonObject userJson){
        return generateToken(userJson, 2 * 24 * 60);
//        return generateToken(userJson, 5);
    }

    public static JsonObject generateToken(JsonObject userJson, int minutes){
        if (userJson == null) {
            throw new NullPointerException("null username is illegal");
        }

        Key key = getKey();
        Date expiry = TokenUtil.getExpiryDate(minutes);
        //String email = userJson.getString("email");
        String userId = userJson.getString("id");

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        String jwtString = Jwts.builder()
            .setIssuer("Jersey-Security-Basic")
            .setSubject(userId)
            .setAudience(userJson.getString("role"))
            .setExpiration(expiry)
            .setIssuedAt(new Date())
            .setId("1")
            .signWith(signatureAlgorithm, key)
            .compact();

        Token token = new Token();
        token.setAuthToken(jwtString);
        token.setExpires(expiry);

        Document document = new Document();
        document.append("auth_token", jwtString);
        document.append("expiry", expiry);
        AccountUtil.updateUserToken(userId, document);

        JsonObjectBuilder tokenBuilder = Json.createObjectBuilder();
        tokenBuilder.add("auth_token", jwtString);
        tokenBuilder.add("expiry", TimeUtil.date2String(expiry));

        return tokenBuilder.build();

//        return tokenJson;
    }

    public static boolean isValid(String token) {
        try {
            Key key = getKey();
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(token.trim());
            String userId = claimsJws.getBody().getSubject();

            if (AccountUtil.checkUserToken(userId, token)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("token check sign failed: " + token);
            return false;
        }
    }

    public static String getName(String jwsToken) {
        Key key = getKey();
        if (isValid(jwsToken)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            return claimsJws.getBody().getSubject();
        }
        return null;
    }
    public static String[] getRoles(String jwsToken) {
        Key key = getKey();
        if (isValid(jwsToken)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            return claimsJws.getBody().getAudience().split(",");
        }
        return new String[]{};
    }

    public static int getVersion(String jwsToken) {
        Key key = getKey();
        if (isValid(jwsToken)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            return Integer.parseInt(claimsJws.getBody().getId());
        }
        return -1;
    }

    public static Date getExpiryDate(int minutes) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutes);

        return calendar.getTime();
    }
}
