package kr.vaiv.sdt.cmmn.domain;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CmmnSearchVo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Long id;

    protected String searchCondition;

    protected String searchKeyword;

}
