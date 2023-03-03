package kr.vaiv.sdt.cmmn.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class CmmnBeanUtilsTest {

  @Test
  public void newInstanceTest() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    Dto dto = CmmnBeanUtils.newInstance(Dto.class);
    System.out.println(dto);
  }

  @Test
  public void copyMapToObjTest() throws Exception {
    Map<String, Object> sourceMap = Map.of("name", "홍길동", "age", 20, "birth", new Date());

    Dto dto = CmmnBeanUtils.copyMapToObj(sourceMap, Dto.class, Map.of("id", "id", "name", "name"));
    System.out.println(dto.getId());
    System.out.println(dto.getName());

    assertEquals(dto.getId(), null);
    assertEquals(dto.getName(), "홍길동");

  }

  @Test
  @DisplayName("uri로 HttpRequest 생성 테스트")
  public void createHttpRequestTest1() throws IOException, InterruptedException {
    String uri = "https://map.duplanet.kr/api/land-rank/getSelectRank";

    HttpRequest request = CmmnUtils.createHttpRequest(uri);
    System.out.println(request);
    System.out.println(request.headers());

    // 자동생성 확인
    assertTrue(!request.uri().toString().contains("?"));

  }

  @Test
  @DisplayName("uri, param으로 HttpRequest 생성 테스트")
  public void createHttpRequestTest2() throws IOException, InterruptedException {
    String uri = "https://map.duplanet.kr/api/land-rank/getSelectRank";
    Map<String, Object> param = Map.of("userSub", "", "totalCnt", 10);

    HttpRequest request = CmmnUtils.createHttpRequest(uri, param);
    System.out.println(request);
    System.out.println(request.headers());

    // 자동생성 확인
    assertTrue(request.uri().toString().contains("?"));
    assertTrue(request.uri().toString().contains("userSub="));
    assertTrue(request.uri().toString().contains("totalCnt=10"));

  }

  @Test
  @DisplayName("uri, param, header로 HttpRequest 생성 테스트")
  public void createHttpRequestTest3() throws IOException, InterruptedException {
    String uri = "https://map.duplanet.kr/api/land-rank/getSelectRank";
    Map<String, Object> param = Map.of("userSub", "", "totalCnt", 10);
    Map<String, Object> header = Map.of("authorization", "Bearer 12345");

    HttpRequest request = CmmnUtils.createGetHttpRequest(uri, param, header);
    System.out.println(request);
    System.out.println(request.headers());

    // 자동생성 확인
    assertTrue(request.uri().toString().contains("?"));
    assertTrue(request.uri().toString().contains("userSub="));
    assertTrue(request.uri().toString().contains("totalCnt=10"));

    assertTrue(request.headers().toString().contains("authorization"));
    assertTrue(request.headers().toString().contains("Bearer 12345"));
  }

  @Test
  @DisplayName("uri로 get 방식 요청 테스트")
  public void requestByGetTest1() throws IOException, InterruptedException {
    String uri = "https://map.duplanet.kr/api/land-rank/getSelectRank";

    String responseString = CmmnUtils.requestByGet(uri);
    System.out.println(responseString);

    assertTrue(responseString.contains("{"));
    assertTrue(responseString.contains("}"));
  }

  @Test
  @DisplayName("uri,param으로 get 방식 요청 테스트")
  public void requestByGetTest2() throws IOException, InterruptedException {
    String uri = "https://map.duplanet.kr/api/land-rank/getSelectRank";
    Map<String, Object> param = Map.of("userSub", "", "totalCnt", 10);

    String responseString = CmmnUtils.requestByGet(uri, param);
    System.out.println(responseString);

    assertTrue(responseString.contains("{"));
    assertTrue(responseString.contains("}"));
  }

  @Test
  @DisplayName("uri,param,header로 get 방식 요청 테스트")
  public void requestByGetTest3() throws IOException, InterruptedException {
    String uri = "https://map.duplanet.kr/api/land-rank/getSelectRank";
    Map<String, Object> param = Map.of("userSub", "", "totalCnt", 10);
    Map<String, Object> header = Map.of("authorization", "Bearer 12345");

    String responseString = CmmnUtils.requestByGet(uri, param, header);
    System.out.println(responseString);

    assertTrue(responseString.contains("{"));
    assertTrue(responseString.contains("}"));
  }

  @Test
  public void dto_인스턴스_동적생성_테스트() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    Dto dto = CmmnBeanUtils.newInstance(Dto.class);
    assertTrue(null != dto);
  }

  @Test
  public void entity_인스턴스_동적생성_테스트() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    Entity entity = CmmnBeanUtils.newInstance(Entity.class);
    assertTrue(null != entity);
  }

  @Test
  public void dto를Entity로변환_테스트() throws Exception {
    // given
    Dto dto = new Dto();
    dto.setId("id");
    dto.setName("네임");

    // when
    Entity entity = CmmnBeanUtils.dtoToEntity(dto, Entity.class);

    // then
    assertEquals("id", entity.getId());
    assertEquals("네임", entity.getName());
    assertTrue(null == entity.getDt());
  }

  @Test
  public void dto를Entity로변환_제외필드포함_테스트() throws Exception {
    // given
    Dto dto = new Dto();
    dto.setId("id");
    dto.setName("네임");

    // when
    Entity entity = CmmnBeanUtils.dtoToEntity(dto, Entity.class, List.of("name"));

    // then
    assertEquals("id", entity.getId());
    assertTrue(null == entity.getName());
    assertTrue(null == entity.getDt());
  }

  @Test
  public void entity를Dto로변환_테스트() throws Exception {
    Entity entity = new Entity();
    entity.setId("아이디");
    entity.setName("네임");
    entity.setDt(new Date());
    // log.debug("entity {}", entity);

    Dto dto = CmmnBeanUtils.entityToDto(entity, Dto.class);

    assertEquals("아이디", dto.getId());
    assertEquals("네임", dto.getName());
  }

  @Test
  public void entity를Dto로변환_제외필드포함_테스트() throws Exception {
    Entity entity = new Entity();
    entity.setId("아이디");
    entity.setName("네임");
    entity.setDt(new Date());
    // log.debug("entity {}", entity);

    Dto dto = CmmnBeanUtils.entityToDto(entity, Dto.class, List.of("name"));

    assertEquals("아이디", dto.getId());
    assertTrue(null == dto.getName());
  }
}

class Dto {
  private String id;
  private String name;

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public Dto() {
    super();
  }
}

class Entity {
  private String id;
  private String name;
  private Date dt;

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDt(Date dt) {
    this.dt = dt;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public Date getDt() {
    return this.dt;
  }

  public Entity() {
    super();
  }
}
