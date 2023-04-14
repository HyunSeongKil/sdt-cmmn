package kr.vaiv.sdt.cmmn.misc;

import java.util.HashMap;
import java.util.Map;

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
   * @param data          데이터
   * @param resultCode    결과 코드
   * @param resultMessage 결과 메시지
   * @return 인스턴스
   */
  public static CmmnResultMap of(Object data, String resultCode, Object resultMessage) {
    CmmnResultMap map = new CmmnResultMap();
    map.put(CmmnConst.DATA, data);
    map.put(CmmnConst.RESULT_CODE, resultCode);
    map.put(CmmnConst.RESULT_MESSAGE, resultMessage);

    return map;
  }

  /**
   * 생성
   * 
   * @param data          데이터
   * @param page          페이지 인덱스. ※주의:0부터 시작
   * @param size          페이지 크기
   * @param totalElements 전체 건수
   * @return
   */
  public static CmmnResultMap of(Object data, Integer page, Integer size, Long totalElements) {
    CmmnResultMap map = new CmmnResultMap();
    map.put(CmmnConst.DATA, data);
    map.put("page", page);
    map.put("size", size);
    map.put("totalElements", totalElements);

    return map;
  }

  /**
   * 생성
   * 
   * @param data          데이터
   * @param pageable      페이지 정보
   * @param totalElements 전체 건수
   * @return
   */
  public static CmmnResultMap of(Object data, Pageable pageable, Long totalElements) {
    return of(data, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
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
  public void putResultCode(String resultCode) {
    this.put(CmmnConst.RESULT_CODE, resultCode);
  }

  /**
   * 결과 메시지 설정
   * 
   * @param resultMessage 결과 메시지
   */
  public void putResultMessage(Object resultMessage) {
    this.put(CmmnConst.RESULT_MESSAGE, resultMessage);
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
  public void putTotalElements(Long totalElements) {
    this.put("totalElements", totalElements);
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
