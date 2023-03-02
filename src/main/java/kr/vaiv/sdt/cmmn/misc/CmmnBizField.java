package kr.vaiv.sdt.cmmn.misc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 필드에 사용되는 어노테이션
 * 
 * @author gravity
 * @since 2020. 8. 10.
 *
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface CmmnBizField {
	/**
	 * 키 여부
	 * 
	 * @return
	 */
	boolean key() default true;

	/**
	 * 키 순서. 동적으로 쿼리 생성할 때 사용. 순서 중요. order순서가 jpa method name만들때 사용됨
	 * 
	 * @return
	 */
	int order() default 0;
}
