package kr.vaiv.sdt.cmmn.domain;

import java.io.Serializable;
import java.util.Date;

import lombok.*;

/**
 * layer간 데이터 전송용. 업무 로직을 갖지 않는 순수한 데이터 객체 getter, setter 메소드만 가짐
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CmmnDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Long id;

    protected Date registDt = new Date();

    protected Date updtDt = new Date();

}
