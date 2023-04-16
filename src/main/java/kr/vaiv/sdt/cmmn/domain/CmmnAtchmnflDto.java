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
public class CmmnAtchmnflDto {
  private String atchmnflId;

  private String atchmnflGroupId;

  private String originalFilename;

  private String saveFilename;

  private Long fileSize;

  private String saveSubPath;

  private String contentType;

  private String registerId;
  private LocalDateTime registDt;
  private String updaterId;
  private LocalDateTime updateDt;

}
