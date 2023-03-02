package kr.vaiv.sdt.cmmn.misc;

import java.util.List;

import kr.vaiv.sdt.cmmn.domain.CmmnDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 페이징 결과 담는 클래스
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class CmmnPageResult {
  /**
   * 데이터 목록
   */
  private List<? extends CmmnDto> data;

  /**
   * 0부터 시작
   */
  @Builder.Default
  private int page = 0;

  /**
   * 페이지 크기
   */
  @Builder.Default
  private int size = 10;

  /**
   * 전체 데이터 건수
   */
  private long totalElements;

}
