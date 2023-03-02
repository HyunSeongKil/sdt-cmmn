/**
 * 
 */
package kr.vaiv.sdt.cmmn.service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.vaiv.sdt.cmmn.domain.CmmnWhereCondition;

/**
 * 모든 interface의 부모
 * 
 * @author gravity
 * 
 */
public interface CmmnService<dto, DTO, IDTYPE> {

	/**
	 * id(pk)로 1건 조회
	 * 
	 * @param id
	 * @return DTO|null
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	DTO findById(IDTYPE id) throws IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

	/**
	 * 전체 데이터 조회
	 * 
	 * @return
	 * @throws Exception
	 */
	List<DTO> findAll() throws Exception;

	/**
	 * 데이터 조회 with 페이징,정렬
	 * 
	 * @param pageable
	 * @return
	 */
	Page<DTO> findAll(Pageable pageable);

	/**
	 * 조회 조건으로 목록 조회
	 * 
	 * @param where
	 * @return
	 */
	List<DTO> findAllByWhere(CmmnWhereCondition where);

	/**
	 * 조회 조건으로 목록 조회
	 * 
	 * @param wheres
	 * @return
	 */
	List<DTO> findAllByWhere(List<CmmnWhereCondition> wheres);

	/**
	 * 조회 조건으로 목록 조회 with 페이징,정렬
	 * 
	 * @param where
	 * @param pageable
	 * @return
	 */
	Page<DTO> findAllByWhere(CmmnWhereCondition where, Pageable pageable);

	/**
	 * 조회 조건으로 목록 조회 with 페이징,정렬
	 * 
	 * @param wheres
	 * @param pageable
	 * @return
	 */
	Page<DTO> findAllByWhere(List<CmmnWhereCondition> wheres, Pageable pageable);

	/**
	 * 조회 조건으로 1건 조회
	 * 
	 * @param where
	 * @return
	 */
	DTO findByWhere(CmmnWhereCondition where);

	/**
	 * bizfield로 1건 조회
	 * 
	 * @param obj
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	DTO findByBizField(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * bizField로 목록 조회하기
	 * 
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	List<DTO> findAllByBizField(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * bizField로 목록 조회하기 with 페이징,정렬
	 * 
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	Page<DTO> findAllByBizField(Object obj, Pageable pageable) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * 1건 삭제
	 * 
	 * @param id
	 */
	void delete(IDTYPE id);

	/**
	 * 비동기로 1건 삭제
	 *
	 * @param id
	 */
	void deleteAsync(IDTYPE id);

	/**
	 * 조회 조건으로 목록 조회. 그 목록을 삭제
	 * 
	 * @param where
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	void deleteByWhere(CmmnWhereCondition where) throws NumberFormatException, IllegalArgumentException, IllegalAccessException;

	/**
	 * 등록
	 * 
	 * @param dto
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	IDTYPE regist(DTO dto) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException;

	/**
	 * 비동기 등록
	 *
	 * @param dto
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 */
	IDTYPE registAsync(DTO dto) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException;

	/**
	 * 수정
	 * 
	 * @param dto
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	void update(DTO dto) throws NumberFormatException, IllegalArgumentException, IllegalAccessException;

	/**
	 * 비동기 수정
	 *
	 * @param dto
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	void updateAsync(DTO dto) throws NumberFormatException, IllegalArgumentException, IllegalAccessException;

	/**
	 * 수정
	 * 
	 * @param id
	 * @param dto
	 * @throws Exception
	 * @since 2021-06-18 기존 항상 컬럼명 id로 조회하던 것을 @id필드로 처리하도록 변경
	 */
	void update(IDTYPE id, DTO dto) throws Exception;

	/**
	 * 조회 조건으로 1건 조회. dto의 값으로 수정
	 * 
	 * @param where
	 * @param dto
	 */
	void updateByWhere(CmmnWhereCondition where, DTO dto);

	/**
	 * 전체 건수 조회
	 */
	Integer getTotcnt();

}
