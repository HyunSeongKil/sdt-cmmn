package kr.vaiv.sdt.cmmn.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import kr.vaiv.sdt.cmmn.domain.CmmnAtchmnflDto;

public interface CmmnAtchmnflService {
  Map<String, String> regist(MultipartFile mfile);

  List<Map<String, String>> regist(List<MultipartFile> mfiles);

  Map<String, String> regist(String atchmnflGroupId, MultipartFile mfile);

  List<Map<String, String>> regist(String atchmnflGroupId, List<MultipartFile> mfiles);

  void deleteById(String atchmnflId);

  void deletesByAtchmnflGroupId(String atchmnflGroupId);

  Optional<CmmnAtchmnflDto> getById(String atchmnflId);

  List<CmmnAtchmnflDto> getsByAtchmnflGroupId(String atchmnflGroupId);

  File getFileById(String atchmnflId);

  List<File> getFilesByAtchmnflGroupId(String atchmnflGroupId);
}
