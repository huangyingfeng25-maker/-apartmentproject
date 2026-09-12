package com.atguigu.lease.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    //token过期时间固定值
    private static long tokenExpiration = 60 * 60 * 1000L;

    //加密秘钥
    private static SecretKey tokenSignKey = Keys.hmacShaKeyFor("M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ".getBytes());

    //生成token方法，参数用户id和用户名称
    public static String createToken(Long userId, String username) {
        String token = Jwts.builder().
                //设置分类
                        setSubject("USER_INFO").
                //设置生成token过期时间
                        setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).
                //jwt负载内容，设置用户信息到token
                        claim("userId", userId).
                claim("username", username).
                //根据秘钥进行加密
                        signWith(tokenSignKey).
                //把生成token压缩
                        compressWith(CompressionCodecs.GZIP).
                compact();
        return token;
    }

    //校验jwttoken，从token获取数据
    public static Claims parseToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder().
                    //设置解密密钥
                            setSigningKey(tokenSignKey).
                    //从解密字符串获取设置负载数据
                            build().parseClaimsJws(token);
            return claimsJws.getBody();

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) {
        //1 生成token
        String token = JwtUtil.createToken(9l, "lucy");
        System.out.println(token);

        //2 解析token
        Claims claims = JwtUtil.parseToken(token);
        Object username = claims.get("username");
        Object userId = claims.get("userId");
        System.out.println(userId+" :: "+username);
    }
}
