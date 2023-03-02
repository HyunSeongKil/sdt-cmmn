/**
 * 
 */
package kr.vaiv.sdt.cmmn.persistence;

/**
 * mybatis용 mapper의 부모
 * 
 * @author gravity
 * @since 2020. 8. 10.
 *
 */
public interface CmmnMapper<ENTITY, IDTYPE> {

	Integer getTotcnt();
}
