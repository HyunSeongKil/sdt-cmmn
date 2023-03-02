package kr.vaiv.sdt.cmmn.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.vaiv.sdt.cmmn.domain.AtchmnflDto;
import lombok.extern.slf4j.Slf4j;

/**
 * 첨부파일 처리 핸들러
 * ! atchmnfl-api의 end point와 이곳에서 호출하는 것은 동일해야 함
 * ! 임시로 파일을 생성하는 경우가 있음. 성능 이슈 발생할 수 있음. 쓰레기 파일 처리해야 함
 * ! stream을 이용해 파일 처리할 수 있는 방안 찾아봐야 함
 */
@Slf4j
public class AtchmnflHandler {
  private String apiUri;

  /** ! 1개를 생성해서 사용해야 함. 이 인스턴스를 계속 생성하면 서버단에서 file open 갯수관련 이슈 발생 */
  private CloseableHttpClient httpClient = null;

  private HttpPost httpPost = null;
  private HttpDelete httpDelete = null;
  private HttpGet httpGet = null;

  public AtchmnflHandler(CloseableHttpClient httpClient, String apiUri) {
    super();
    this.httpClient = httpClient;
    this.apiUri = apiUri;

    if (httpClient == null) {
      throw new RuntimeException("httpClient is null");
    }
    if (CmmnUtils.isEmpty(apiUri)) {
      throw new RuntimeException("apiUri is empty");
    }
  }

  private HttpPost getHttpPostInstance() {
    if (this.httpPost == null) {
      this.httpPost = new HttpPost(apiUri);
    }

    return this.httpPost;
  }

  private HttpDelete getHttpDeleteInstance() {
    if (httpDelete == null) {
      this.httpDelete = new HttpDelete();
    }

    return this.httpDelete;
  }

  private HttpGet getHttpGetInstance() {
    if (this.httpGet == null) {
      this.httpGet = new HttpGet();
    }

    return this.httpGet;
  }

  /**
   * 첨부파일 정보 조회
   * 
   * @param atchmnflId 첨부파일 아이디(pk)
   * @return
   * @throws IOException
   * @throws ClientProtocolException
   */
  @SuppressWarnings("unchecked")
  public Optional<AtchmnflDto> getById(String atchmnflId) throws ClientProtocolException, IOException {
    HttpGet httpGet = getHttpGetInstance();
    httpGet.setURI(URI.create(apiUri + "/" + atchmnflId));

    try (CloseableHttpResponse response = this.httpClient.execute(httpGet)) {
      String str = EntityUtils.toString(response.getEntity());
      Map<String, Object> responseMap = new ObjectMapper().readValue(str, Map.class);
      Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

      return Optional.ofNullable(CmmnBeanUtils.copyMapToObj(dataMap, AtchmnflDto.class));
    }

  }

  @SuppressWarnings("unchecked")
  public List<AtchmnflDto> getsByAtchmnflGroupId(String atchmnflGroupId) throws ClientProtocolException, IOException {
    HttpGet httpGet = getHttpGetInstance();
    httpGet.setURI(URI.create(apiUri + "/atchmnfl-groups/" + atchmnflGroupId + "/atchmnfls"));

    try (CloseableHttpResponse response = this.httpClient.execute(httpGet)) {
      String str = EntityUtils.toString(response.getEntity());
      Map<String, Object> responseMap = new ObjectMapper().readValue(str, Map.class);
      if (!responseMap.containsKey("data")) {
        return List.of();
      }

      if (responseMap.get("data").getClass() != List.class &&
          responseMap.get("data").getClass() != ArrayList.class) {
        return List.of();
      }

      List<Map<String, Object>> maps = (List) responseMap.get("data");

      return maps
          .stream()
          .map(map -> CmmnBeanUtils.copyMapToObj(map, AtchmnflDto.class))
          .collect(Collectors.toList());
    }

  }

  /**
   * atchmnflGroupId로 첫번째 파일 추출
   * 
   * @see getFileById(String)
   * 
   * @param atchmnflGroupId 첨부파일 그룹 아이디
   * @return
   * @throws IOException
   * @throws ClientProtocolException
   */
  public Optional<File> getFirstFileByAtchmnflGroupId(String atchmnflGroupId)
      throws ClientProtocolException, IOException {
    return getsByAtchmnflGroupId(atchmnflGroupId)
        .stream()
        .findFirst()
        .map(dto -> {
          try {
            return getFileById(dto.getAtchmnflId());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .get();
  }

  public List<File> getFilesByAtchmnflGroupId(String atchmnflGroupId) throws ClientProtocolException, IOException {
    return this.getsByAtchmnflGroupId(atchmnflGroupId)
        .stream()
        .map(dto -> {
          try {
            Optional<File> opt = getFileById(atchmnflGroupId);
            if (opt.isPresent()) {
              return opt.get();
            }

            return null;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }

  /**
   * 외
   * 서버에 파일 요청 후 서버의 임시 경로에 파일을 저장한 후 그 파일을 리턴함
   * ! TODO 일정 시간 후 임시로 생성된 파일을 삭제해야 함
   * 
   * @param atchmnflId 첨부파일 아이디(pk)
   * @return
   * @throws IOException
   * @throws ClientProtocolException
   */
  public Optional<File> getFileById(String atchmnflId) throws ClientProtocolException, IOException {
    Optional<AtchmnflDto> opt = getById(atchmnflId);
    if (opt.isEmpty()) {
      return Optional.empty();
    }

    AtchmnflDto dto = opt.get();

    HttpGet httpGet = getHttpGetInstance();
    httpGet.setURI(URI.create(apiUri + "/" + atchmnflId + "/download"));

    try (CloseableHttpResponse response = this.httpClient.execute(httpGet)) {
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        return Optional.empty();
      }

      File destFile = CmmnUtils
          .getTempPath()
          .resolve(atchmnflId + "_" + dto.getSaveFilename())
          .toFile();

      try (FileOutputStream fos = new FileOutputStream(destFile)) {
        entity.writeTo(fos);
      }

      return Optional.of(destFile);
    }

  }

  /*
   * 첨부파일 삭제
   * 
   * @param atchmnflId
   */
  public void deleteById(String atchmnflId) {
    HttpDelete httpDelete = getHttpDeleteInstance();
    httpDelete.setURI(URI.create(apiUri + "/" + atchmnflId));

    try {
      this.httpClient.execute(httpDelete);
    } catch (IOException e) {
      log.error("{}", e);
    }
  }

  /**
   * atchmnflGroupId로 n개 파일 삭제
   * 
   * @param atchmnflGroupId
   */
  public void deletesByAtchmnflGroupId(String atchmnflGroupId) throws ClientProtocolException, IOException {
    getsByAtchmnflGroupId(atchmnflGroupId)
        .stream()
        .forEach(dto -> deleteById(dto.getAtchmnflId()));
  }

  /**
   * 파일 등록
   * 파일과 텍스트를 같이 전달
   * 첨부파일 그룹 아이디를 신규로 생성함
   * 
   * @param mfiles
   * @param textMap key/value {"key":"값"} => &key=값
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<AtchmnflDto> regist(List<MultipartFile> mfiles, Map<String, String> textMap) throws Exception {
    //
    Function<List<Map<String, Object>>, List<AtchmnflDto>> mapsToDtos = (atchmnflMaps) -> {
      return atchmnflMaps
          .stream()
          .map(m -> CmmnBeanUtils.copyMapToObj(m, AtchmnflDto.class))
          .collect(Collectors.toList());
    };

    //
    Consumer<List<Map<String, Object>>> deleteAtchmnfls = (atchmnflMaps) -> {
      atchmnflMaps
          .stream()
          .forEach(map -> {
            deleteById(map.get("atchmnflId").toString());
          });
    };

    //
    Consumer<List<File>> deleteTmpFiles = (tmpFiles) -> {
      tmpFiles
          .stream()
          .forEach(tmpFile -> {
            if (tmpFile.exists()) {
              tmpFile.delete();
            }
          });
    };

    //
    Function<List<MultipartFile>, List<File>> mfilesToTmpFiles = (mfiles2) -> {
      return mfiles2
          .stream()
          .map(mfile -> {
            File tmpFile = CmmnUtils
                .getTempPath()
                .resolve(mfile.getOriginalFilename())
                .toFile();

            try {
              mfile.transferTo(tmpFile);
            } catch (IllegalStateException | IOException e) {
              throw new RuntimeException(e);
            }

            return tmpFile;
          })
          .collect(Collectors.toList());

    };

    //
    BiFunction<List<File>, Map<String, String>, MultipartEntityBuilder> createMultipartEntityBuilder = (tmpFiles,
        map) -> {
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();

      tmpFiles
          .stream()
          .forEach(destFile -> {
            builder.addBinaryBody("files", destFile);
          });

      map
          .keySet()
          .forEach(key -> {
            builder.addTextBody(key, textMap.get(key));
          });

      return builder;
    };

    ////
    //
    List<File> tmpFiles = mfilesToTmpFiles.apply(mfiles);

    //
    MultipartEntityBuilder builder = createMultipartEntityBuilder.apply(tmpFiles, textMap);

    //
    HttpPost httpPost = getHttpPostInstance();
    httpPost.setEntity(builder.build());

    //
    List<Map<String, Object>> atchmnflMaps = null;

    //
    try (CloseableHttpResponse response = this.httpClient.execute(httpPost)) {
      HttpEntity resEntity = response.getEntity();
      String content = EntityUtils.toString(resEntity, "UTF-8");

      Map<String, Object> map = new ObjectMapper().readValue(content, HashMap.class);
      atchmnflMaps = (List<Map<String, Object>>) map.get(CmmnConst.DATA);

      return mapsToDtos.apply(atchmnflMaps);

    } catch (Exception e) {
      if (atchmnflMaps != null) {
        deleteAtchmnfls.accept(atchmnflMaps);
      }

      throw e;

    } finally {
      // 임시 파일 삭제
      deleteTmpFiles.accept(tmpFiles);
    }
  }

  /**
   * 신규로 첨부파일 그룹아이디 생성 후 파일 등록
   * 
   * @param mfiles
   * @param textMap {"key":"value"} => key=value
   * @param bizType
   * @return
   * @throws Exception
   */
  public List<AtchmnflDto> regist(List<MultipartFile> mfiles, Map<String, String> textMap, String bizType)
      throws Exception {
    Map<String, String> map = new HashMap<>();
    Iterator<String> iter = textMap.keySet().iterator();

    while (iter.hasNext()) {
      String key = iter.next();
      map.put(key, textMap.get(key));
    }

    map.put("bizType", bizType);

    return regist(mfiles, map);
  }

  /**
   * 존재하는 atchmnflGroupId에 파일 등록
   * 
   * @param mfiles
   * @param bizType
   * @param textMap         {"key":"값"}
   * @param atchmnflGroupId 첨부파일 그룹 아이디
   * @return
   * @throws Exception
   */
  public List<AtchmnflDto> regist(List<MultipartFile> mfiles, Map<String, String> textMap, String bizType,
      String atchmnflGroupId) throws Exception {
    Map<String, String> map = new HashMap<>();
    Iterator<String> iter = textMap.keySet().iterator();

    while (iter.hasNext()) {
      String key = iter.next();
      map.put(key, textMap.get(key));
    }

    map.put("bizType", bizType);
    map.put("atchmnflGroupId", atchmnflGroupId);

    return regist(mfiles, map);
  }
}
