/**
 * 
 */
package kr.vaiv.sdt.cmmn.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import kr.vaiv.sdt.cmmn.domain.CmmnWhereCondition;
import kr.vaiv.sdt.cmmn.domain.CmmnWhereOp;
import kr.vaiv.sdt.cmmn.misc.CmmnBeanUtils;
import kr.vaiv.sdt.cmmn.misc.CmmnBizField;
import kr.vaiv.sdt.cmmn.misc.CmmnUtils;
import kr.vaiv.sdt.cmmn.persistence.CmmnMapper;
import kr.vaiv.sdt.cmmn.service.CmmnService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 모든 service impl 의 부모
 * 
 * @author gravity
 *
 */
@Slf4j
public class CmmnServiceImpl<JPA, MAPPER, ENTITY, DTO, IDTYPE> implements CmmnService<ENTITY, DTO, IDTYPE> {
	/**
	 * jpa
	 */
	private JpaRepository<ENTITY, IDTYPE> _repo;

	/**
	 * mybatis mapper
	 */
	private CmmnMapper<ENTITY, IDTYPE> _mapper;
	/**
	 * 엔티티
	 */
	private ENTITY _entity;

	/**
	 * dto
	 */
	private DTO _dto;

	/**
	 * entity manager
	 */
	@PersistenceContext
	private EntityManager em;

	@PostConstruct
	private void init() {
		log.info("<< {}", this);
	}

	/**
	 * entity 인스턴스 생성 & dto의 값 entity로 복사
	 * 단, dto의 필드명과 entity의 필드명 같아야 함
	 * 예) dtoToEntity(userDto, UserEntity.class)
	 * 예) dtoToEntity(userDto, UserEntity.class, "id")
	 * 
	 * @param dtoObj       dto 인스턴스
	 * @param exceptFields 변환에서 제외할 필드 목록. 옵션
	 * @return entity 인스턴스
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected ENTITY dtoToEntity(DTO dtoObj, String... exceptFields) {
		try {
			return (ENTITY) CmmnBeanUtils.dtoToEntity(dtoObj, _entity.getClass(),
					null == exceptFields ? List.of() : List.of(exceptFields));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * dto 인스턴스 생성 & entity의 값 dto로 복사
	 * 단, entity의 필드명과 dto의 필드명 같아야 함
	 * 예) entityToDto(userEntity, UserDto.class)
	 * 예) entityToDto(userEntity, UserDto.class, "id")
	 * 
	 * @param entityObj    entity 인스턴스
	 * @param exceptFields 변환에서 제외할 필드 목록. 옵션
	 * @return dto 인스턴스
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected DTO entityToDto(ENTITY entityObj, String... exceptFields) {
		try {
			return (DTO) CmmnBeanUtils.entityToDto(entityObj, _dto.getClass(),
					null == exceptFields ? List.of() : List.of(exceptFields));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Transactional
	public void delete(IDTYPE id) {
		//
		this._repo.deleteById(id);
	}

	@Async
	@Override
	public void deleteAsync(IDTYPE id) {
		//
		delete(id);
	}

	/**
	 * 동적으로 dto 생성
	 * 
	 * @return {Object|null} 생성 실패하면 널 리턴
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private DTO createDto() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		return (DTO) _dto.getClass().getConstructor().newInstance();
	}

	/**
	 * 엔티티 목록을 dto 목록으로 변환
	 */
	private List<DTO> entitiesToDtos(Iterable<ENTITY> entities) {
		if (null == entities) {
			return null;
		}

		List<DTO> dtos = new ArrayList<>();

		entities.forEach(entity -> {
			try {
				dtos.add(entityToDto(entity));
			} catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});

		return dtos;
	}

	/**
	 * entity의 값을 dto로 바인딩. TODO 나중에 ModelMapper, @NotNull를 이용하여 refactoring하기.
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	private DTO entityToDto(ENTITY entity) throws IllegalAccessException, InstantiationException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (null == entity) {
			return null;
		}

		// dto생성
		DTO newDto = createDto();

		// dto 기준으로 루프
		CmmnBeanUtils.getFields(newDto).forEach(dtoField -> {
			String fieldNameOfDto = dtoField.getName();
			// log.debug("dtoField:{}", dtoField);

			boolean b = CmmnBeanUtils.existsField(entity.getClass(), fieldNameOfDto);
			if (!b) {
				return;
			}

			try {
				// entity의 값을 dto에 할당
				Object v = CmmnBeanUtils.getFieldValue(entity, fieldNameOfDto);
				CmmnBeanUtils.setFieldValue(newDto, fieldNameOfDto, v);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

		return (DTO) newDto;
	}

	@Override
	public List<DTO> findAll() throws Exception {
		List<ENTITY> entities = this._repo.findAll();

		return entitiesToDtos(entities);
	}

	@Override
	public Page<DTO> findAll(Pageable pageable) {
		Page<ENTITY> entities = this._repo.findAll(pageable);

		List<DTO> dtos = entitiesToDtos(entities);

		return new PageImpl<>(dtos, pageable, entities.getTotalElements());
	}

	/**
	 * 페이징을 위한 메서드
	 * 
	 * @autho break8524@vaiv.com
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Page<DTO> findAllPgByStartPg(Integer startPage, Integer contentsSize) {
		PageRequest pageRequest = PageRequest.of(startPage, contentsSize, Sort.Direction.DESC, "id");
		return this.findAll(pageRequest);
	}

	private List<ENTITY> findEntitiesByWhere(CmmnWhereCondition where) {
		BuilderQueryRoot<ENTITY> bqr = getQuery();
		bqr = getQueryByWhere(bqr, List.of(where));

		return em.createQuery(bqr.getQuery()).getResultList();
	}

	/**
	 * entity의 bizkey로 테이블 조회하여 entity구하기
	 * 
	 * @param dto
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	private ENTITY findByBizKey(Optional<ENTITY> opt, List<FieldAndOrder> bizFields)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException {

		if (!opt.isPresent()) {
			return null;
		}

		ENTITY entity = opt.get();
		Collections.sort(bizFields, new FieldAndOrderComparator());

		//
		String methodName = createMethodName("findBy", bizFields);

		//
		Class<?>[] paramTypes = getParamTypes(bizFields);

		//
		Object[] paramValues;
		paramValues = getParamValues(bizFields, entity);
		//
		if (existsMethod(methodName)) {
			// repo의 메소드 실행
			Object obj = _repo.getClass().getMethod(methodName, paramTypes).invoke(this._repo, paramValues);

			//
			// log.debug("<<.findByBizKey - {}", obj);
			return (ENTITY) obj;

		}

		return findByBizKeyByCreateQuery(bizFields, paramValues);

	}

	/**
	 * entity에서 @id 사용한 필드 구하기
	 * 
	 * @return
	 */
	private Field getIdField(ENTITY entity) {
		Field[] fields = entity.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];

			if (null != f.getAnnotation(Id.class)) {
				return f;
			}
		}

		return null;
	}

	@Override
	public DTO findById(IDTYPE id) throws IllegalAccessException, InstantiationException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		Optional<ENTITY> opt = this._repo.findById(id);

		if (opt.isPresent()) {
			return entityToDto(opt.get());
		}

		return null;
	}

	@Override
	public Integer getTotcnt() {
		return _mapper.getTotcnt();
	}

	@Override
	@Transactional
	@SuppressWarnings(value = "unchecked")
	public IDTYPE regist(DTO dto) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstantiationException {
		Optional<ENTITY> opt = toEntity(dto);
		if (opt.isEmpty()) {
			return null;
		}

		ENTITY entity = opt.get();

		try {
			CmmnBeanUtils.setFieldValue(entity, "registDt", new Date());
			CmmnBeanUtils.setFieldValue(entity, "updtDt", new Date());

			// 저장
			entity = this._repo.save(entity);

			// @id 필드 추출
			Field idField = getIdField(entity);
			if (null == idField) {
				return null;
			}

			// @id 필드의 값 리턴
			return (IDTYPE) CmmnBeanUtils.getFieldValue(entity, idField.getName());

		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

	@Async
	@Override
	public IDTYPE registAsync(DTO dto) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InstantiationException {
		return regist(dto);
	}

	@Override
	public void update(DTO dto) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		update(getId(dto), dto);
	}

	@Async
	@Override
	public void updateAsync(DTO dto) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		update(getId(dto), dto);
	}

	@Override
	@Transactional
	public void update(IDTYPE id, DTO dto) throws IllegalArgumentException, IllegalAccessException {

		// id 필드 추출
		Field idField = getIdField(_entity);
		idField.setAccessible(true);

		List<ENTITY> entities = findEntitiesByWhere(
				CmmnWhereCondition.of(idField.getName(), CmmnBeanUtils.getFieldValue(dto, idField.getName())));

		//
		if (CmmnUtils.isEmpty(entities)) {
			return;
		}

		//
		ENTITY entity = entities.get(0);

		// dto값 entity로 복사
		CmmnBeanUtils.getFields(dto).forEach(f -> {
			try {
				String fieldName = f.getName();

				if ("updtDt".equals(fieldName)) {
					CmmnBeanUtils.setFieldValue(entity, fieldName, new Date());
					return;
				}

				Object v = CmmnBeanUtils.getFieldValue(dto, fieldName);
				CmmnBeanUtils.setFieldValue(entity, fieldName, v);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// 저장
		this._repo.save(entity);

		// this._repo.findById(id).ifPresent(entity -> {
		// CmmnBeanUtils.getFields(dto).forEach(f -> {
		// try {
		// String fieldName = f.getName();

		// if ("updtDt".equals(fieldName)) {
		// CmmnBeanUtils.setFieldValue(entity, fieldName, new Date());
		// return;
		// }

		// Object v = CmmnBeanUtils.getFieldValue(dto, fieldName);
		// CmmnBeanUtils.setFieldValue(entity, fieldName, v);

		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }
		// });

		// // log.debug("update - {} {} {}", id, dto, entity);
		// this._repo.save(entity);
		// });
	}

	/**
	 * Field[]에서 fieldName해당하는 필드가 classes중 하나라도 존재하는지 여부
	 * 
	 * @param fieldName
	 * @param classes
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean hasAnyAnnotation(String fieldName, Class<? extends Annotation>... classes) {
		Field[] fields = _entity.getClass().getDeclaredFields();

		for (Field f : fields) {
			if (!f.getName().equals(fieldName)) {
				continue;
			}

			for (Class clz : classes) {
				if (f.isAnnotationPresent(clz)) {
					return true;
				}
			}
		}

		//
		return false;
	}

	/**
	 * entity에 bizkey 존재 여부
	 * 
	 * @param opt
	 * @return true(domain에 @CmmnBizField(key=true) 존재) / false
	 */
	private boolean existsBizKey(Optional<ENTITY> opt) {
		return (0 < getBizFields(opt).size());
	}

	/**
	 * dto의 toEntity() 호출
	 * 
	 * @param dto
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	private Optional<ENTITY> toEntity(DTO dto) throws NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Method method = dto.getClass().getMethod("toEntity", null);
		return Optional.ofNullable((ENTITY) method.invoke(dto, null));
	}

	/**
	 * japrepo에 methodName에 해당하는 메소드가 존재하는지 여부
	 * 
	 * @param methodName
	 * @return
	 */
	private boolean existsMethod(String methodName) {
		Method[] methods = this._repo.getClass().getMethods();
		if (CmmnUtils.isEmpty(methods)) {
			return false;
		}

		//
		Method method = Arrays.stream(methods).filter(m -> m.getName().equals(methodName)).findFirst().orElse(null);

		// log.debug("<<.existsMethod - {}", (null != method));
		return (null != method);
	}

	/**
	 * bizFields 로 동적으로 쿼리 생성 & 값 추출
	 * 
	 * @param bizFields   biz fields 목록
	 * @param paramValues 파라미터 값 배열
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @since 20200821 init
	 */
	@SuppressWarnings("unchecked")
	private ENTITY findByBizKeyByCreateQuery(List<FieldAndOrder> bizFields, Object[] paramValues)
			throws IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		List<?> list = em.createQuery(getQuery(bizFields, paramValues)).getResultList();
		if (CmmnUtils.isEmpty(list)) {
			log.debug("<<.findByBizKeyByCreateQuery - empty list");
			return null;
		}

		//
		log.debug("<<.findByBizKeyByCreateQuery - {}", list.get(0));
		return (ENTITY) list.get(0);
	}

	/**
	 * 업무키 필드 목록 조회
	 * 
	 * @param opt
	 * @return
	 */
	private List<FieldAndOrder> getBizFields(Optional<ENTITY> opt) {
		//
		List<FieldAndOrder> bizFields = new ArrayList<>();

		if (!opt.isPresent()) {
			log.warn("<<.getBizFields - null domain");
			return bizFields;
		}

		//
		List<Field> fields = new ArrayList<>();
		CmmnUtils.bindFieldsUpTo(opt.getClass(), fields);

		if (CmmnUtils.isEmpty(fields)) {
			log.warn("<<.getBizFields - empty fields");
			return bizFields;
		}

		//
		fields.forEach(f -> {
			if (null == f) {
				return;
			}

			//
			CmmnBizField bizField = f.getAnnotation(CmmnBizField.class);

			if (null == bizField) {
				return;
			}

			if (!bizField.key()) {
				return;
			}

			// 컬럼명
			String column = f.getName();
			Column c = f.getAnnotation(Column.class);
			if (null != c) {
				column = c.name();
			}

			// 업무키 존재하면 추가
			bizFields.add(FieldAndOrder.builder().field(f).column(column).order(bizField.order()).build());

		});

		//
		log.debug("<<.getBizFields - {}", bizFields.size());
		return bizFields;
	}

	/**
	 * field명으로 메소드명(문자열) 생성 메소드명 규칙 : pre + 필드명[ + And + 필드명...]
	 * 
	 * @param pre
	 * @param fields
	 * @return
	 */
	private String createMethodName(String pre, List<FieldAndOrder> fields) {
		String str = "";
		for (FieldAndOrder e : fields) {
			e.field.setAccessible(true);

			if (CmmnUtils.isNotEmpty(str)) {
				str += "And";
			}
			str += StringUtils.capitalize(e.field.getName());
		}

		//
		log.debug("<<.getMethodName - {}", pre + str);
		return pre + str;
	}

	/**
	 * field 목록으로 각 field의 파라미터 타입 배열 생성
	 * 
	 * @param fields
	 * @return
	 */
	private Class<?>[] getParamTypes(List<FieldAndOrder> fields) {
		if (CmmnUtils.isEmpty(fields)) {
			return new Class[] {};
		}

		//
		Class<?>[] classes = new Class[fields.size()];
		//
		for (int i = 0; i < fields.size(); i++) {
			classes[i] = fields.get(i).field.getType();
		}

		//
		// log.debug("<<.getParamTypes - {}", classes);
		return classes;
	}

	/**
	 * field목록으로 파라미터 값 목록 생성
	 * 
	 * @param list
	 * @param entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private Object[] getParamValues(List<FieldAndOrder> list, Object entity)
			throws IllegalArgumentException, IllegalAccessException {
		if (CmmnUtils.isEmpty(list)) {
			return new Object[] {};
		}

		//
		Object[] objects = new Object[list.size()];
		for (int i = 0; i < list.size(); i++) {
			objects[i] = list.get(i).field.get(entity);
		}

		//
		log.debug("<<.getParamValues - {}", objects);
		return objects;
	}

	/**
	 * 동적으로 쿼리 생성
	 * 
	 * @param bizFields   업무필드 목록
	 * @param paramValues 각 업무필드별 값
	 * @return 쿼리
	 * @since 20200821 20200827 bug fix
	 */
	@Deprecated
	@SuppressWarnings({ "unchecked" })
	private CriteriaQuery<ENTITY> getQuery(List<FieldAndOrder> bizFields, Object[] paramValues) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ENTITY> q = (CriteriaQuery<ENTITY>) builder.createQuery(_entity.getClass());
		Root<ENTITY> root = (Root<ENTITY>) q.from(_entity.getClass());

		//
		q.select(root);

		//
		if (CmmnUtils.isNull(bizFields)) {
			em.createQuery(q).getResultList();
		}

		// where조건 갯수
		Predicate[] predicates = new Predicate[bizFields.size()];

		for (int i = 0; i < bizFields.size(); i++) {
			FieldAndOrder f = bizFields.get(i);

			//
			predicates[i] = builder.equal(root.get(f.field.getName()), paramValues[i]);
		}

		//
		q.where(predicates);

		//
		log.debug("<<.getQuery - {}", q);
		return q;
	}

	/**
	 * 정상적으로 동작하기는 하나, 어느 정도의 하드코딩이 맘에 들지 않음. getQuery()사용하기를 추천함 동적으로 sql문 생성
	 * 
	 * @param idFields  @Id 필드 목록
	 * @param bizFields @DsField 필드 목록
	 * @return
	 */
	@Deprecated
	private String getSql(List<FieldAndOrder> idFields, List<FieldAndOrder> bizFields) {

		String sql = "";

		sql = " SELECT 1 AS dummy";

		// get id column
		if (CmmnUtils.isNotEmpty(idFields)) {
			for (FieldAndOrder f : idFields) {
				sql += ", t." + f.field.getName();
			}
		}

		//
		if (CmmnUtils.isNotEmpty(bizFields)) {
			for (FieldAndOrder f : bizFields) {
				sql += ", t." + f.field.getName();
			}
		}

		//
		sql += " FROM " + this._entity.getClass().getSimpleName() + " t";
		sql += " WHERE 1=1";

		//
		if (CmmnUtils.isNotEmpty(bizFields)) {
			for (FieldAndOrder f : bizFields) {
				sql += " AND " + f.field.getName() + " = :" + f.field.getName();
			}
		}

		//
		return sql;
	}

	/**
	 * 수정 불가 컬럼인지 여부
	 * 
	 * @param f 필드
	 * @return
	 */
	private boolean isNotUpdatableField(Field f) {
		return Arrays.asList(new String[] { "id", "registDt", "updtDt" }).contains(f.getName());
	}

	/**
	 * obj의 id값 구하기
	 * 
	 * @param obj
	 * @return
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	private IDTYPE getId(Object obj) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		return (IDTYPE) Long.valueOf("" + CmmnBeanUtils.getFieldValue(obj, "id"));
	}

	/**
	 * 쿼리문 동적 생성. 조회조건 없이 모든 데이터 조회
	 * 
	 * @return
	 */
	private BuilderQueryRoot<ENTITY> getQuery() {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ENTITY> query = (CriteriaQuery<ENTITY>) builder.createQuery(_entity.getClass());
		Root<ENTITY> root = (Root<ENTITY>) query.from(_entity.getClass());

		//
		query.select(root);

		return new BuilderQueryRoot<ENTITY>(builder, query, root);
	}

	/**
	 * 초기값 세팅
	 * 
	 * @param j
	 * @param m
	 * @param entity
	 * @param dto
	 */
	@SuppressWarnings("unchecked")
	protected void set(JPA j, MAPPER m, ENTITY entity, DTO dto) {
		this._repo = (JpaRepository<ENTITY, IDTYPE>) j;
		this._mapper = (CmmnMapper<ENTITY, IDTYPE>) m;
		this._entity = (ENTITY) entity;
		this._dto = (DTO) dto;
	}

	@Override
	public DTO findByWhere(CmmnWhereCondition where) {
		BuilderQueryRoot<ENTITY> bqr = getQueryByWhere(where);

		List<ENTITY> entities = em.createQuery(bqr.getQuery()).getResultList();
		List<DTO> dtos = entitiesToDtos(entities);

		return CmmnUtils.isEmpty(dtos) ? null : dtos.get(0);
	}

	@Override
	public DTO findByBizField(@NotNull Object obj) throws IllegalArgumentException, IllegalAccessException {
		List<CmmnWhereCondition> wheres = createWhereConditionByBizField(obj);
		if (CmmnUtils.isEmpty(wheres)) {
			return null;
		}

		List<DTO> dtos = findAllByWhere(wheres);
		if (CmmnUtils.isEmpty(dtos)) {
			return null;
		}

		return dtos.get(0);
	}

	/**
	 * 조건으로 조회
	 * 
	 * @param bqr
	 * @param wheres 조건 목록
	 * @return
	 */
	private BuilderQueryRoot<ENTITY> getQueryByWhere(BuilderQueryRoot<ENTITY> bqr, List<CmmnWhereCondition> wheres) {
		// log.debug(">> getQueryByWhere - {}", wheres);

		CriteriaBuilder builder = bqr.getBuilder();
		CriteriaQuery query = bqr.getQuery();
		Root root = bqr.getRoot();

		// where조건 갯수
		List<Predicate> predicates = new ArrayList<>();

		wheres.forEach(where -> {
			if (CmmnUtils.isEmpty(where.getValue())) {
				return;
			}

			if (CmmnWhereOp.EQUAL == where.getWhereOp()) {
				predicates.add(builder.equal(root.get(where.getColumnName()), where.getValue()));
			}
			if (CmmnWhereOp.LIKE == where.getWhereOp()) {
				predicates.add(builder.like(root.get(where.getColumnName()), "%" + where.getValue() + "%"));
			}
		});

		//
		query.where(predicates.toArray(new Predicate[predicates.size()]));

		return bqr;
	}

	/**
	 * 조건으로 조회
	 * 
	 * @param bqr
	 * @param where 조회
	 * @return
	 */
	private BuilderQueryRoot<ENTITY> getQueryByWhere(BuilderQueryRoot<ENTITY> bqr, CmmnWhereCondition where) {
		return getQueryByWhere(bqr, List.of(where));
	}

	/**
	 * 조건으로 조회
	 * 
	 * @param where 조회
	 * @return
	 */
	private BuilderQueryRoot<ENTITY> getQueryByWhere(CmmnWhereCondition where) {
		BuilderQueryRoot<ENTITY> bqr = getQuery();

		return getQueryByWhere(bqr, where);
	}

	@Override
	public List<DTO> findAllByWhere(CmmnWhereCondition where) {
		BuilderQueryRoot<ENTITY> bqr = getQueryByWhere(where);
		List<ENTITY> entities = em.createQuery(bqr.getQuery()).getResultList();

		return entitiesToDtos(entities);
	}

	@Override
	public List<DTO> findAllByWhere(List<CmmnWhereCondition> wheres) {
		BuilderQueryRoot<ENTITY> bqr = getQuery();
		bqr = getQueryByWhere(bqr, wheres);

		List<ENTITY> entities = em.createQuery(bqr.getQuery()).getResultList();

		return entitiesToDtos(entities);
	}

	@Override
	public Page<DTO> findAllByWhere(CmmnWhereCondition where, Pageable pageable) {

		return findAllByWhere(List.of(where), pageable);
	}

	@Override
	public Page<DTO> findAllByWhere(List<CmmnWhereCondition> wheres, Pageable pageable) {
		BuilderQueryRoot<ENTITY> bqr = getQuery();

		bqr = getQueryByWhere(bqr, wheres);

		bqr = getQueryBySort(bqr, pageable.getSort());

		Page<ENTITY> entities = getResultListByPg(bqr, pageable);

		List<DTO> dtos = entitiesToDtos(entities);

		return new PageImpl<>(dtos, pageable, entities.getTotalElements());
	}

	/**
	 * 페이징,정렬
	 * 
	 * @param bqr
	 * @param pageable
	 * @return
	 */
	private Page<ENTITY> getResultListByPg(BuilderQueryRoot<ENTITY> bqr, Pageable pageable) {

		List<ENTITY> entities = null;
		int count = 0;

		if (pageable.isPaged()) {
			entities = em.createQuery(bqr.getQuery()).setFirstResult((int) pageable.getOffset())
					.setMaxResults(pageable.getPageSize()).getResultList();
			count = em.createQuery(bqr.getQuery()).getResultList().size();
		} else {
			entities = em.createQuery(bqr.getQuery()).getResultList();
			count = entities.size();
		}

		return new PageImpl<>(entities, pageable, count);
	}

	/**
	 * 정렬
	 * 
	 * @param bqr
	 * @param sort 정렬
	 * @return
	 */
	private BuilderQueryRoot<ENTITY> getQueryBySort(BuilderQueryRoot<ENTITY> bqr, Sort sort) {
		if (!sort.isSorted()) {
			return bqr;
		}

		CriteriaBuilder builder = bqr.getBuilder();
		CriteriaQuery query = bqr.getQuery();
		Root<ENTITY> root = bqr.getRoot();

		query.orderBy(QueryUtils.toOrders(sort, root, builder));

		return bqr;
	}

	@Override
	@Transactional
	public void updateByWhere(CmmnWhereCondition where, DTO dto) {
		List<ENTITY> entities = findEntitiesByWhere(where);

		entities.forEach(entity -> {
			new ModelMapper().map(dto, entity);

			this._repo.save(entity);
		});

	}

	@Override
	@Transactional
	public void deleteByWhere(CmmnWhereCondition where)
			throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		List<ENTITY> entities = findEntitiesByWhere(where);

		entities.forEach(entity -> {
			try {
				this._repo.deleteById(getId(entity));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

	}

	/**
	 * obj의 BizField 어노테이션으로 List<WhereCondition> 생성
	 * 
	 * @param obj vo | dto | entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private List<CmmnWhereCondition> createWhereConditionByBizField(@NotNull Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = obj.getClass().getDeclaredFields();
		if (CmmnUtils.isEmpty(fields)) {
			return List.of();
		}

		List<CmmnWhereCondition> wheres = new ArrayList<>();

		Field f;
		for (int i = 0; i < fields.length; i++) {
			f = fields[i];

			if (null == f.getAnnotation(CmmnBizField.class)) {
				continue;
			}

			f.setAccessible(true);
			wheres.add(CmmnWhereCondition.of(f.getName(), f.get(obj)));
		}

		return wheres;
	}

	@Override
	public Page<DTO> findAllByBizField(@NotNull Object obj, @NotNull Pageable pageable)
			throws IllegalArgumentException, IllegalAccessException {
		List<CmmnWhereCondition> wheres = createWhereConditionByBizField(obj);
		if (CmmnUtils.isEmpty(wheres)) {
			return new PageImpl(List.of(), pageable, 0);
		}

		return findAllByWhere(wheres, pageable);
	}

	@Override
	public List<DTO> findAllByBizField(@NotNull Object obj) throws IllegalArgumentException, IllegalAccessException {

		List<CmmnWhereCondition> wheres = createWhereConditionByBizField(obj);

		if (CmmnUtils.isEmpty(wheres)) {
			return List.of();
		}

		return findAllByWhere(wheres);
	}
}

/**
 * 
 */
@Builder
class FieldAndOrder {
	/**
	 * 필드
	 */
	Field field;
	/**
	 * table의 컬럼 명
	 */
	String column;

	/**
	 * 순서
	 */
	int order;
}

/**
 * FieldAndOrder를 order순으로 정렬
 * 
 * @author gravity
 * @since 2020. 8. 10.
 *
 */
class FieldAndOrderComparator implements Comparator<FieldAndOrder> {

	@Override
	public int compare(FieldAndOrder o1, FieldAndOrder o2) {
		return o1.order - o2.order;
	}

}

/**
 * 빌더, 쿼리, 루트 holder
 */
@Getter
@Setter
class BuilderQueryRoot<ENTITY> {
	/**
	 * 빌더
	 */
	private CriteriaBuilder builder;

	/**
	 * 루트
	 */
	private Root<ENTITY> root;

	/**
	 * 쿼리
	 */
	private CriteriaQuery<ENTITY> query;

	/**
	 * 생성자
	 * 
	 * @param builder 빌더
	 * @param query   쿼리
	 * @param root    루트
	 */
	public BuilderQueryRoot(CriteriaBuilder builder, CriteriaQuery<ENTITY> query, Root<ENTITY> root) {
		this.builder = builder;
		this.query = query;
		this.root = root;
	}
}
