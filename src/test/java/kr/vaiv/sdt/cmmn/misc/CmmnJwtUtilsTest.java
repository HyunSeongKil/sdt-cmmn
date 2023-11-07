package kr.vaiv.sdt.cmmn.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

public class CmmnJwtUtilsTest {

  @Test
  void testCreateToken() {
    Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put("id", "아이디");

    Date exp = new Date(2023, 0, 1, 0, 0, 0);
    exp.setTime(exp.getTime() + (1000 * 60 * 1));
    System.out.println(exp);

    String token = CmmnJwtUtils.createToken(CmmnJwtUtils.SECRET_KEY, bodyMap, exp);
    System.out.println(token);

    String[] arr = token.split("\\.");
    assertEquals(3, arr.length);

    // assertEquals(36, arr[0].length());
    // assertEquals(71, arr[1].length());
    // assertEquals(43, arr[2].length());
  }

  @Test
  void testCreateToken2() {
    Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put("id", "아이디");

    Date exp = new Date(2023, 0, 1, 0, 0, 0);
    exp.setTime(exp.getTime() + (1000 * 60 * 1));
    System.out.println(exp);

    String token = CmmnJwtUtils.createToken(bodyMap, exp);
    System.out.println(token);

    String[] arr = token.split("\\.");
    assertEquals(3, arr.length);

    assertEquals(20, arr[0].length());
    assertEquals(48, arr[1].length());
    assertEquals(43, arr[2].length());
  }

  @Test
  void testCreateToken3() {
    Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put("id", "아이디");

    String token = CmmnJwtUtils.createToken(bodyMap);
    System.out.println(token);

  }

  @Test
  void testGetBody() {
    Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put("id", "아이디");

    String token = CmmnJwtUtils.createToken(bodyMap);
    System.out.println(token);

    Map<String, Object> body = CmmnJwtUtils.getBody(token);
    System.out.println(body);

    assertEquals("아이디", body.get("id"));

  }

  @Test
  @Deprecated
  void testValidateToken() {
    Map<String, Object> bodyMap = new HashMap<>();
    bodyMap.put("id", "아이디");

    String token = CmmnJwtUtils.createToken(bodyMap);
    System.out.println(token);

    boolean b = CmmnJwtUtils.validateToken(CmmnJwtUtils.SECRET_KEY, token);
    assertEquals(true, b);
  }

  @Test
  void testCreateSecretKey() {
    SecretKey secretKey = CmmnJwtUtils.createSecretKey(CmmnJwtUtils.SECRET_KEY);
    // System.out.println(secretKey.getAlgorithm());

    assertEquals("HmacSHA256", secretKey.getAlgorithm());
  }
}
