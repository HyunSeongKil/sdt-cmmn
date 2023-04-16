package kr.vaiv.sdt.cmmn.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmmnAtchmnflGroupDto {
  private String atchmnflGroupId;
  private String registerId;
  private LocalDateTime registDt;
  private String updaterId;
  private LocalDateTime updateDt;
}
