package kr.vaiv.sdt.cmmn.misc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

public class CmmnResultMapTest {

  @Test
  void ofTest() {
    CmmnResultMap resultMap = CmmnResultMap.of(List.of(), PageRequest.of(0, 10), 0L);
    System.out.println(resultMap);

    assertTrue(0 == ((List) resultMap.get(CmmnConst.DATA)).size());
    assertTrue(0 == (Integer) resultMap.get("page"));
    assertTrue(10 == (Integer) resultMap.get("size"));
    assertTrue(0L == (Long) resultMap.get("totalElements"));
  }
}
