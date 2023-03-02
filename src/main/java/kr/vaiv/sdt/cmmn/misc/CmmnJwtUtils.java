package kr.vaiv.sdt.cmmn.misc;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * jwt 관련 유틸리티
 */
public class CmmnJwtUtils {

  /**
   * default secret key
   */
  public static final String SECRET_KEY = "hyunseongkil,aha1492@outlook.kr,01055411492";

  /**
   * 토큰 생성
   * 만료시간 기본 10분으로 설정
   * 
   * @see createToken(String, Map, Date)
   * @param bodyMap
   * @return
   */
  public static String createToken(Map<String, Object> bodyMap) {
    Date exp = new Date();
    exp.setTime(exp.getTime() + (1000 * 60 * 10)); // 10min

    return createToken(SECRET_KEY, bodyMap, exp);
  }

  /**
   * 토큰 생성
   * 
   * @see createToken(String, Map, Date)
   * @param bodyMap
   * @param expireDate
   * @return
   */
  public static String createToken(Map<String, Object> bodyMap, Date expireDate) {
    return createToken(SECRET_KEY, bodyMap, expireDate);
  }

  /**
   * 토큰 생성
   * ! 주의 : bodyMap은 immutation이 아니어야 함
   * 
   * @param secretKey 비밀키
   * @param bodyMap   body에 들어갈 값
   * @param exp       만료시간
   * @return
   */
  public static String createToken(String secretKey, Map<String, Object> bodyMap, Date exp) {

    //
    Map<String, Object> headerMap = new HashMap<>();
    headerMap.put("typ", "JWT");
    headerMap.put("alg", "HS256");

    //
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    //
    String token = Jwts.builder()
        .setHeader(headerMap)
        .setClaims(bodyMap)
        .setExpiration(exp)
        .signWith(signatureAlgorithm, signingKey)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .compact();

    //
    log.info("body: {} token: {}", bodyMap, token);
    return token;

  }

  /**
   * 토큰 검사
   * 
   * @param secretKey
   * @param token
   * @return
   */
  public static boolean validateToken(String secretKey, String token) {
    try {
      getBody(secretKey, token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 토큰 검사
   * 
   * @see validateToken(String, String)
   * @param token
   * @return
   */
  public static boolean validateToken(String token) {
    return validateToken(SECRET_KEY, token);
  }

  /**
   * body값 구하기
   * 
   * @param token
   * @return
   */
  public static Map<String, Object> getBody(String token) {
    return getBody(SECRET_KEY, token);
  }

  /**
   * body값 구하기
   * 
   * @param secretKey
   * @param token
   * @return
   */
  public static Map<String, Object> getBody(String secretKey, String token) {
    try {
      Claims claims = Jwts.parser()
          .setSigningKey(DatatypeConverter.parseBase64Binary(secretKey))
          .parseClaimsJws(token)
          .getBody();

      //
      Map<String, Object> map = new HashMap<>();
      Iterator<String> iter = claims.keySet().iterator();
      while (iter.hasNext()) {
        String key = iter.next();

        map.put(key, claims.get(key));
      }

      //
      return map;

    } catch (ExpiredJwtException exception) {
      log.error("{}", "jwt 시간 만료");
      throw exception;
    } catch (JwtException exception) {
      log.error("{}", "jwt 오류");
      throw exception;
    }
  }
}
