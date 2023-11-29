package kr.vaiv.sdt.cmmn.misc;

import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.bcel.Const;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * result map
 * 
 * @since 20210721
 */
public class CmmnResultMap extends HashMap<String, Object> {

  /**
   * 빈값 리턴
   * 
   * @return
   */
  public static CmmnResultMap empty() {
    return withData(Map.of());
  }

  /**
   * 생성 with 데이터
   * 
   * @see 클린코드 p32
   * @param data 데이터
   * @return
   * @since 20220203
   */
  public static CmmnResultMap withData(Object data) {
    return of(data);
  }

  /**
   * 생성
   * 
   * @param data 데이터
   * @return 인스턴스
   */
  public static CmmnResultMap of(Object data) {
    CmmnResultMap map = new CmmnResultMap();
    map.put(CmmnConst.DATA, data);
    return map;
  }

  /**
   * 생성
   * 
   * @param data       데이터
   * @param resultCode 결과 코드
   * @return 인스턴스
   */
  public static CmmnResultMap of(Object data, String resultCode) {
    CmmnResultMap map = new CmmnResultMap();
    map.put(CmmnConst.DATA, data);
    map.put(CmmnConst.RESULT_CODE, resultCode);
    return map;
  }

  /**
   * 생성
   * 
   * @param data    데이터
   * @param code    결과 코드
   * @param message 결과 메시지
   * @return 인스턴스
   */
  public static CmmnResultMap of(Object data, String code, Object message) {
    CmmnResultMap map = new CmmnResultMap();
    map.put(CmmnConst.DATA, data);
    map.put(CmmnConst.RESULT_CODE, code);
    map.put(CmmnConst.RESULT_MESSAGE, message);

    map.put(CmmnConst.CODE, code);
    map.put(CmmnConst.MESSAGE, message);

    return map;
  }

  /**
   * 생성
   * 
   * @param data       데이터
   * @param page       페이지 인덱스. ※주의:0부터 시작
   * @param size       페이지 크기
   * @param totalCount 전체 건수
   * @return
   */
  public static CmmnResultMap of(Object data, Integer page, Integer size, Long totalCount) {
    CmmnResultMap map = new CmmnResultMap();
    map.put(CmmnConst.DATA, data);
    map.put("page", page);
    map.put("size", size);
    map.put("totalElements", totalCount);

    map.put(CmmnConst.TOTAL_COUNT, totalCount);

    return map;
  }

  /**
   * 생성
   * 
   * @param data       데이터
   * @param pageable   페이지 정보
   * @param totalCount 전체 건수
   * @return
   */
  public static CmmnResultMap of(Object data, Pageable pageable, Long totalCount) {
    return of(data, pageable.getPageNumber(), pageable.getPageSize(), totalCount);
  }

  /**
   * 데이터 설정
   * 
   * @param data 데이터
   */
  public void putData(Object data) {
    this.put(CmmnConst.DATA, data);
  }

  /**
   * 결과 코드 설정
   * 
   * @param resultCode 결과 코드
   */
  @Deprecated(since = "20231129 ", forRemoval = true)
  public void putResultCode(String resultCode) {
    this.put(CmmnConst.RESULT_CODE, resultCode);
  }

  public void putCode(String code) {
    this.put(CmmnConst.CODE, code);
  }

  /**
   * 결과 메시지 설정
   * 
   * @param resultMessage 결과 메시지
   */
  @Deprecated(since = "20231129 ", forRemoval = true)
  public void putResultMessage(Object resultMessage) {
    this.put(CmmnConst.RESULT_MESSAGE, resultMessage);
  }

  public void putMessage(Object message) {
    this.put(CmmnConst.MESSAGE, message);
  }

  /**
   * 페이지 인덱스 설정
   * 
   * @param page 페이지 인덱스. 주의 0부터 시작
   */
  public void putPage(Integer page) {
    this.put("page", page);
  }

  /**
   * 페이지 크기 설정
   * 
   * @param size 페이지 크기
   */
  public void putSize(Integer size) {
    this.put("size", size);
  }

  /**
   * 전체 갯수 설정
   * 
   * @param totalElements 전체 개수
   */
  @Deprecated
  public void putTotalElements(Long totalElements) {
    this.put("totalElements", totalElements);
  }

  public void putTotalCount(Long totalCount) {
    this.put(CmmnConst.TOTAL_COUNT, totalCount);
  }

  /**
   * @deprecated from 1.23.0308
   * @see CmmnBeanUtils.toJsonString(Object)
   * @return
   * @throws JsonProcessingException
   */
  public String toJsonString() throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(this);
  }
}
