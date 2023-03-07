package kr.vaiv.sdt.cmmn.misc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.reflections.*;
import org.reflections.scanners.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * bean 관련 유틸
 */
@Slf4j
public class CmmnBeanUtils {

  /**
   * 동적으로 인스턴스 생성
   * 
   * @param <T>
   * @param t
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   */
  public static <T> T newInstance(Class<T> t) throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    return (T) t.getConstructor().newInstance();
  }

  /**
   * entity 인스턴스 생성 & entity 필드의 값을 dto의 값으로 설정. 단, dto필드명과 entity필드명이 같아야지만 값 설정
   * 가능함
   * 
   * @see fromDtoToEntity(DTO, Class<ENTITY>, List<String>)
   * @param <DTO>
   * @param <ENTITY>
   * @param dtoObj      dto 인스턴스
   * @param entityClass entity 클래스
   * @return 생성된 entity 인스턴스
   * @throws Exception
   */
  public static <DTO, ENTITY> ENTITY dtoToEntity(DTO dtoObj, Class<ENTITY> entityClass) throws Exception {
    return dtoToEntity(dtoObj, entityClass, List.of());
  }

  /**
   * entity 인스턴스 생성 & entity 필드의 값을 dto의 값으로 설정. 단, dto필드명과 entity필드명이 같아야지만 값 설정
   * 가능함
   * 
   * 예) CmmnBeanUtils.<UserDto, UserEntity>fromDtoToEntity(userDto,
   * UserEntity.class)
   * 
   * @param <DTO>
   * @param <ENTITY>
   * @param dtoObj           dto 인스턴스
   * @param entityClass      entity 클래스
   * @param exceptFieldNames 설정하지 않을 필드명 목록
   * @return 생성된 entity 인스턴스
   * @throws Exception
   */
  public static <DTO, ENTITY> ENTITY dtoToEntity(DTO dtoObj, Class<ENTITY> entityClass, List<String> exceptFieldNames)
      throws Exception {
    if (null == dtoObj || null == entityClass || null == exceptFieldNames) {
      throw new RuntimeException("null paramter exists");
    }

    //
    ENTITY entityObj = newInstance(entityClass);

    Field[] dtoFields = dtoObj.getClass().getDeclaredFields();

    getFields(entityObj).forEach(f -> {
      String entityFieldName = f.getName();

      if (!existsField(dtoFields, entityFieldName)) {
        return;
      }

      if (exceptFieldNames.contains(entityFieldName)) {
        return;
      }

      try {
        // entity에 값 설정
        setFieldValue(entityObj, entityFieldName, getFieldValue(dtoObj, entityFieldName));
      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.debug("{}", e);
      }
    });

    return entityObj;
  }

  /**
   * dto 인스턴스 생성 & 엔티티의 값으로 dto에 설정. 단, entity의 필드명과 dto의 필드명이 같아야지만 값 설정 가능함
   * 
   * @see fromEntityToDto(ENTITY, Class<DTO>, List<String>)
   * @param <ENTITY>  엔티티의 generic type
   * @param <DTO>     dto의 generic type
   * @param entityObj 엔티티 인스턴스
   * @param dtoClass  dto 클래스
   * @return 생성된 dto 인스턴스
   * @throws Exception
   */
  public static <ENTITY, DTO> DTO entityToDto(ENTITY entityObj, Class<DTO> dtoClass) {
    return entityToDto(entityObj, dtoClass, List.of());
  }

  /**
   * dto 인스턴스 생성 & dto의 값을 엔티티의 값으로 설정
   * 
   * 예) CmmnBeanUtils.<UserEntity, UserDto>fromEntityToDto(userEntity,
   * UserDto.class)
   * 
   * @param <ENTITY>         엔티티의 generic type
   * @param <DTO>            dto의 generic type
   * @param entityObj        엔티티 인스턴스
   * @param dtoClass         dto 클래스
   * @param exceptFieldNames dto 인스턴스에 설정하지 않을 필드명 목록
   * @return 생성된 dto 인스턴스
   * @throws Exception
   */
  public static <ENTITY, DTO> DTO entityToDto(ENTITY entityObj, Class<DTO> dtoClass, List<String> exceptFieldNames) {
    if (null == entityObj || null == dtoClass || null == exceptFieldNames) {
      throw new RuntimeException("null paramter exists");
    }

    //
    DTO dtoObj;
    try {
      dtoObj = newInstance(dtoClass);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e.getMessage());
    }

    Field[] entityFields = entityObj.getClass().getDeclaredFields();

    getFields(dtoObj).forEach(f -> {
      String dtoFieldName = f.getName();

      if (!existsField(entityFields, dtoFieldName)) {
        return;
      }

      if (exceptFieldNames.contains(dtoFieldName)) {
        return;
      }

      try {
        // dto에 값 설정
        setFieldValue(dtoObj, dtoFieldName, getFieldValue(entityObj, dtoFieldName));
      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.debug("{}", e);
      }
    });

    return dtoObj;
  }

  /**
   * from의 필드 값을 to로 복사
   * 
   * @param froms
   * @param tos
   * @param fieldNames
   */
  public static void copyFieldValue(List<?> froms, List<?> tos, String... fieldNames) {
    if (CmmnUtils.isEmpty(froms) || CmmnUtils.isEmpty((tos)) || CmmnUtils.isEmpty(fieldNames)) {
      return;
    }

    try {
      for (int j = 0; j < fieldNames.length; j++) {
        String fieldName = fieldNames[j];

        for (int i = 0; i < froms.size(); i++) {
          Object v = CmmnBeanUtils.getFieldValue(froms.get(i), fieldName);
          CmmnBeanUtils.setFieldValue(tos.get(i), fieldName, v);
        }
      }
    } catch (Exception e) {
      log.error("{}", e);
    }
  }

  /**
   * 필드 리턴 바로 위 부모 필드까지 검색
   * 
   * @param obj       인스턴스
   * @param fieldName 필드명
   * @return
   */
  public static Field getField(Object obj, String fieldName) {
    // 현재 인스턴스
    Field[] fields = obj.getClass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      if (f.getName().equals(fieldName)) {
        return f;
      }
    }

    // 부모
    fields = obj.getClass().getSuperclass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      if (f.getName().equals(fieldName)) {
        return f;
      }
    }

    return null;
  }

  /**
   * fields에 targetFieldName이 존재하는지 여부
   * 
   * @param fields          필드 목록
   * @param targetFieldName 존재여부를 판단할 필드 명
   * @return
   */
  public static boolean existsField(Field[] fields, String targetFieldName) {
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      if (f.getName().equals(targetFieldName)) {
        return true;
      }
    }

    return false;
  }

  /**
   * clz에 fieldName이 존재하는지 여부 바로 위 부모 클래스까지 검사
   * 
   * @param clz       클래스
   * @param fieldName 존재여부를 판단할 필드명
   * @return
   */
  public static boolean existsField(Class<?> clz, String fieldName) {
    Field[] fields = clz.getDeclaredFields();
    if (CmmnUtils.isEmpty(fields)) {
      return false;
    }

    boolean b = existsField(fields, fieldName);
    if (b) {
      return b;
    }

    // 부모
    b = existsField(clz.getSuperclass().getDeclaredFields(), fieldName);

    return b;
  }

  /**
   * 필드 목록 리턴. 1단계 위 부모 필드 목록도 포함
   * 
   * @param obj 엔티티
   * @return fieldName목록. 오류발생|field가 없으면 빈 목록 리턴
   * @since 20200811 init
   */
  public static Set<String> getFieldNames(Object obj) {
    Set<String> names = new HashSet<>();

    if (null == obj) {
      return names;
    }

    //
    Field[] fields = obj.getClass().getDeclaredFields();
    Arrays.asList(fields).forEach(f -> {
      names.add(f.getName());
    });

    // 부모
    fields = obj.getClass().getSuperclass().getDeclaredFields();
    Arrays.asList(fields).forEach(f -> {
      names.add(f.getName());
    });

    return names;
  }

  /**
   * reflection이용. domain의 fieldName의 값을 value로 설정 field가 없거나 오류 발생하면 아무런값도 set하지
   * 않음
   * 
   * @param obj       엔티티
   * @param fieldName 필드명
   * @param value     값
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @since 20200811 init
   */
  public static void setFieldValue(Object obj, String fieldName, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    Field f = getField(obj, fieldName);
    if (null == f) {
      return;
    }

    f.setAccessible(true);
    f.set(obj, value);

  }

  /**
   * reflection이용. domain의 fieldName의 값 추출
   * 
   * @param obj       엔티티
   * @param fieldName 필드명
   * @return field의 값. 필드없거나 오류 발생하면 null 리턴
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @since 20200811 init
   */
  public static Object getFieldValue(Object obj, String fieldName)
      throws IllegalArgumentException, IllegalAccessException {
    boolean b = existsField(obj.getClass(), fieldName);
    if (!b) {
      return null;
    }

    Field f = getField(obj, fieldName);
    f.setAccessible(true);
    return f.get(obj);
  }

  /**
   * 필드 목록 리턴
   * 
   * @param clz 클래스
   * @return
   */
  public static List<Field> getFields(Class clz) {
    List<Field> list = new ArrayList<>();

    if (null == clz) {
      return list;
    }

    Field[] fields = clz.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      Field f = fields[i];

      list.add(f);
    }

    return list;
  }

  /**
   * 필드 목록 리턴. 바로 위 클래스의 필드까지 리턴
   * 
   * @param obj
   * @return
   */
  public static List<Field> getFields(Object obj) {
    List<Field> list = new ArrayList<>();

    if (null == obj) {
      return list;
    }

    list.addAll(getFields(obj.getClass()));
    // 부모
    list.addAll(getFields(obj.getClass().getSuperclass()));

    return list;
  }

  /**
   * @see CmmnBeanUtils#copy(Object, Class, List)
   * @param fromObj
   * @param toClass
   * @return
   */
  public static Object copy(Object fromObj, Class<?> toClass) {
    return copy(fromObj, toClass, List.of());
  }

  /**
   * 오브젝트 복사. 예)
   * <code>UserDto dto = CmmnBeanUtils.copy(repo.findById(userId), UserDto.class) </code>
   * 
   * @param fromObj  원본 오브젝트
   * @param toClass  대상 클래스 예)User.class
   * @param excludes 복사하지 않을 필드명 목록. 필드명이 정확하게 일치해야 함(equal 조건)
   */
  public static Object copy(Object fromObj, Class<?> toClass, List<String> excludes) {
    List<Field> fromFields = getFields(fromObj);
    List<Field> toFields = getFields(toClass);

    Object toObj = null;

    //
    try {
      Constructor<?> ctr = toClass.getDeclaredConstructor();
      toObj = ctr.newInstance();

      //
      toFields.forEach(f -> {
        // 원본에 없는 필드이면
        if (!existsField(f, fromFields)) {
          return;
        }

        // 복사하지 않는 필드이면
        if (excludes.contains(f.getName())) {
          return;
        }

        try {
          f.setAccessible(true);
          f.set(fromObj, f.getName());
        } catch (IllegalArgumentException | IllegalAccessException e) {
          log.error("{}", e);
        }

      });
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      log.error("{}", e);
    }

    return toObj;

  }

  /**
   * fields에 f가 존재하는 여부. 필드명으로만 검사
   * 
   * @param f      찾으려는 필드
   * @param fields 대상 필드 목록
   * @return 존재하면 true
   */
  public static boolean existsField(Field f, List<Field> fields) {
    if (null == f || null == fields) {
      return false;
    }

    for (Field field : fields) {
      if (f.getName().equals(field.getName())) {
        return true;
      }
    }

    return false;
  }

  /**
   * basePackage하위의 전체 클래스 목록 조회
   * 
   * @param basePackage 시작 패키지명
   * @return 클래스 목록
   */
  public static List<Class<?>> getAllClasses(String basePackage) {
    List<Class<?>> classes = new ArrayList<>();

    new Reflections(basePackage, new SubTypesScanner(false)).getAllTypes().forEach(x -> {
      try {
        classes.add(Class.forName(x));
      } catch (ClassNotFoundException e) {
        log.error("{}", e);
      }
    });

    return classes;

  }

  /**
   * srcMap의 값을 destClass로 복사
   * map -> class key/value는 destClass의 field명을 기준으로 함. srcMap의 key와 destClass의
   * field명이 동일해야 함
   * 
   * @param <T>
   * @param srcMap
   * @param destClass
   * @return
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static <T> T copyMapToObj(Map<String, Object> srcMap, Class<T> destClass)
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    Map<String, String> mappingMap = new HashMap<>();

    Field[] fields = destClass.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      mappingMap.put(fields[i].getName(), fields[i].getName());
    }

    return copyMapToObj(srcMap, destClass, mappingMap);
  }

  /**
   * sourceMap의 값을 targetClass로 복사
   * 
   * @param <T>
   * @param srcMap
   * @param destClass
   * @param mappingMap key:sourceMap의 키, value:targetClass의 필드명
   * @return
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  public static <T> T copyMapToObj(Map<String, Object> srcMap, Class<T> destClass,
      Map<String, String> mappingMap)
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

    class InnerClass {
      boolean containsKey(Map<String, Object> fromMap, String key) {
        return fromMap.containsKey(key);
      }

      boolean containsField(T destObj, String fieldName) {
        Field f = null;
        try {
          f = destObj.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException e) {
          return false;
        }

        return null != f;
      }

      void setFieldValue(T destObj, String fieldName, Object value)
          throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = getField(destObj, fieldName);
        if (field == null) {
          return;
        }

        field.setAccessible(true);
        Class<?> fieldType = field.getType();
        Class<?> valueType = value.getClass();

        // 값 타입이 integer이고 필드 타입이 long이면 integer를 long으로 변환하여 저장
        if (fieldType == Long.class && valueType == Integer.class) {
          CmmnBeanUtils.setFieldValue(destObj, fieldName, Long.parseLong(value.toString()));
          return;
        }

        // System.out.println(fieldName + "\t" + fieldType + "\t" + valueType);
        CmmnBeanUtils.setFieldValue(destObj, fieldName, value);

      }
    }
    ;//

    if (null == srcMap || srcMap.isEmpty()) {
      return null;
    }

    T destObj = null;
    try {
      destObj = newInstance(destClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (null == destObj) {
      return null;
    }

    InnerClass ic = new InnerClass();
    Iterator<Entry<String, String>> iter = mappingMap.entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, String> entry = iter.next();

      if (!ic.containsKey(srcMap, entry.getKey())) {
        continue;
      }

      if (!ic.containsField(destObj, entry.getValue())) {
        continue;
      }

      ic.setFieldValue(destObj, entry.getValue(), srcMap.get(entry.getKey()));
    }

    return destObj;
  }

  /**
   * object를 map으로 복사
   * 
   * @param obj
   * @return
   */
  public static Map<String, Object> copyObjectToMap(Object obj) {
    Map<String, Object> map = new HashMap<>();

    Field[] fields = obj.getClass().getDeclaredFields();

    for (Field f : fields) {
      try {
        f.setAccessible(true);

        map.put(f.getName(), f.get(obj));

      } catch (IllegalArgumentException | IllegalAccessException e) {
        log.error("{}", e);
      }
    }

    return map;
  }

  public static String toJsonString(Object obj) throws JsonProcessingException {
    if (CmmnUtils.isNull(obj)) {
      return "";
    }

    return new ObjectMapper().writeValueAsString(obj);

  }
}
