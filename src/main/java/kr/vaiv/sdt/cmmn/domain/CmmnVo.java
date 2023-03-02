package kr.vaiv.sdt.cmmn.domain;

import java.io.Serializable;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * equals(), hashcode() 필수 각 필드는 readonly이어야 함 getter, setter를 가질 수 있음 테이블 내에 있는
 * 속성 외에 추가적인 속성을 가질 수 있음 값을 변경될 수 없음(불편, immutable)
 */
@Getter
@Setter
public class CmmnVo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Long id;

    protected String registDt;

    protected String updtDt;

    @Override
    public boolean equals(Object o) {

        if (null == o) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        return Objects.equals(id, ((CmmnVo) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
