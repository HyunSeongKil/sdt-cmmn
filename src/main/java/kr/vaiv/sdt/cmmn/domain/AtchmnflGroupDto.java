package kr.vaiv.sdt.cmmn.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @deprecated 20230416
 * @see CmmnAtchmnflGroupDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtchmnflGroupDto {
  private String atchmnflGroupId;
  private String bizType;
}
