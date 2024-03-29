package kr.vaiv.sdt.cmmn.domain;

import lombok.*;

/**
 * @deprecated 20230416
 * @see CmmnAtchmnflDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtchmnflDto {
  private String atchmnflId;

  private String atchmnflGroupId;

  private String originalFilename;

  private String saveFilename;

  private Long fileSize;

  private String saveSubPath;

  private String contentType;

  private String deleteAt;
}
