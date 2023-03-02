package kr.vaiv.sdt.cmmn.domain;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CmmnWhereCondition {
    @NotNull
    private String columnName;
    @NotNull
    private Object value;
    @NotNull
    private CmmnWhereOp whereOp = CmmnWhereOp.EQUAL;


    /**
     * equal조건으로 1개 생성
     * @param columnName 컬럼명
     * @param value 값
     * @return
     */
    public static CmmnWhereCondition of(String columnName, Object value){
        return of(columnName, value, CmmnWhereOp.EQUAL);
    }


    /**
     * equal조건으로 2개 생성
     * @param columnName 컬럼명
     * @param value 값
     * @param columnName2 컬럼명2
     * @param value2 값2
     * @return
     */
    public static List<CmmnWhereCondition> of(String columnName, Object value, 
                                        String columnName2, Object value2){
        return List.of(of(columnName, value, CmmnWhereOp.EQUAL),
                    of(columnName2, value2, CmmnWhereOp.EQUAL));
    }

    
    /**
     * equal조건으로 3개 생성
     * @param columnName 컬럼명
     * @param value 값
     * @param columnName2 컬럼명2
     * @param value2 값2
     * @param columnName3 컬럼명3
     * @param value3 값3
     * @return
     */
    public static List<CmmnWhereCondition> of(String columnName, Object value, 
                                        String columnName2, Object value2, 
                                        String columnName3, Object value3){
        return List.of(of(columnName, value, CmmnWhereOp.EQUAL),
                    of(columnName2, value2, CmmnWhereOp.EQUAL),
                    of(columnName3, value3, CmmnWhereOp.EQUAL));
    }

    /**
     * equal조건으로 4개 생성
     * @param columnName 컬럼명
     * @param value 값
     * @param columnName2 컬럼명2
     * @param value2 값2
     * @param columnName3 컬럼명3
     * @param value3 값3
     * @param columnName4 컬럼명4
     * @param value4 값4
     * @return
     */
    public static List<CmmnWhereCondition> of(String columnName, Object value, 
                                        String columnName2, Object value2, 
                                        String columnName3, Object value3, 
                                        String columnName4, Object value4){
        return List.of(of(columnName, value, CmmnWhereOp.EQUAL),
                    of(columnName2, value2, CmmnWhereOp.EQUAL),
                    of(columnName3, value3, CmmnWhereOp.EQUAL),
                    of(columnName4, value4, CmmnWhereOp.EQUAL));
    }

    /**
     * 1개 생성 factory pattern(static of method pattern)
     * @param columnName
     * @param value
     * @param whereOp
     * @return
     */
    public static CmmnWhereCondition of(String columnName, Object value, CmmnWhereOp whereOp){
        return CmmnWhereCondition.builder()
                        .columnName(columnName)
                        .value(value)
                        .whereOp(whereOp)
                        .build();

    }



    /**
     * 2개 생성 factory pattern(static of method pattern)
     * @param columnName
     * @param value
     * @param whereOp
     * @param columnName2
     * @param value2
     * @param whereOp2
     * @return
     */
    public static List<CmmnWhereCondition> of(String columnName, Object value, CmmnWhereOp whereOp, 
                                    String columnName2, Object value2, CmmnWhereOp whereOp2){
        return List.of(of(columnName, value, whereOp), 
                        of(columnName2, value2, whereOp2));        
    }


    /**
     * 3개 생성 factory pattern(static of method pattern)
     * @param columnName
     * @param value
     * @param whereOp
     * @param columnName2
     * @param value2
     * @param whereOp2
     * @param columnName3
     * @param value3
     * @param whereOp3
     * @return
     */
    public static List<CmmnWhereCondition> of(String columnName, Object value, CmmnWhereOp whereOp, 
                                    String columnName2, Object value2, CmmnWhereOp whereOp2,
                                    String columnName3, Object value3, CmmnWhereOp whereOp3){
        return List.of(of(columnName, value, whereOp), 
                        of(columnName2, value2, whereOp2),
                        of(columnName3, value3, whereOp3));        
    }



    /**
     * 4개 생성 factory pattern(static of method pattern)
     * @param columnName
     * @param value
     * @param whereOp
     * @param columnName2
     * @param value2
     * @param whereOp2
     * @param columnName3
     * @param value3
     * @param whereOp3
     * @param columnName4
     * @param value4
     * @param whereOp4
     * @return
     */
    public static List<CmmnWhereCondition> of(String columnName, Object value, CmmnWhereOp whereOp, 
                                    String columnName2, Object value2, CmmnWhereOp whereOp2,
                                    String columnName3, Object value3, CmmnWhereOp whereOp3,
                                    String columnName4, Object value4, CmmnWhereOp whereOp4){
        return List.of(of(columnName, value, whereOp), 
                        of(columnName2, value2, whereOp2),
                        of(columnName3, value3, whereOp3),
                        of(columnName4, value4, whereOp4));        
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
