package com.example.classroomschool.util.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;

public class TokenUtil {

  private static final long EXPIRE_TIME = 10 * 60 * 60 * 1000; // token 到期时间10小时

  private static final String TOKEN_SECRET = "ljdyaishijin**3nkjnj??";            //设置密钥

  private static Date expireDate;

  /**
   * 生成token
   */
  public static String sign(String role, String account) {
    String token = null;
    try {
      Date expireAt = new Date(System.currentTimeMillis() + EXPIRE_TIME); // 创建过期时间
      expireDate = expireAt;
      token = JWT.create().withClaim("role", role)        // 存放用户角色
          .withIssuer("auth0")    // 发行人
          .withClaim("username", account)    // 存放账号
          .withExpiresAt(expireAt)                          // 过期时间
          .sign(Algorithm.HMAC256(TOKEN_SECRET));
    } catch (IllegalArgumentException | JWTCreationException e) {
      e.printStackTrace();
    }
    return token;
  }

  /**
   * token验证
   */
  public static Boolean verify(String token) {
    try {
      // 创建 token 验证器
      JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET))
          .withIssuer("auth0")
          .build();
      jwtVerifier.verify(token);
    } catch (IllegalArgumentException | JWTVerificationException e) {
      // 抛出错误即为验证不通过
      return false;
    }
    return true;
  }

  /**
   * 验证token是否过期
   **/
  public static Boolean isTokenExpired(String token) {
    try {
      return expireDate.before(new Date());
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 获取账号
   **/
  public static String getAccount(String token) {
    JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET))
        .withIssuer("auth0")
        .build();//创建token验证器
    DecodedJWT decodedjwt = jwtVerifier.verify(token);
    return decodedjwt.getClaim("username").asString();

  }


  /**
   * 获取角色
   **/
  public static String getRole(String token) {
    JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET))
        .withIssuer("auth0")
        .build();//创建token验证器
    DecodedJWT decodedjwt = jwtVerifier.verify(token);
    return decodedjwt.getClaim("role").asString();
  }
}
