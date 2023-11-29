package kr.vaiv.sdt.cmmn.misc;

public class CmmnConst {

    public static final long MIN_1 = 1000 * 60;
    public static final long MIN_10 = MIN_1 * 10;
    public static final long MIN_30 = MIN_10 * 3;
    public static final long MIN_60 = MIN_30 * 2;

    public static final String Y = "Y";
    public static final String N = "N";

    @Deprecated(since = "20231129 @see kr.vaiv.sdt.cmmn.misc.CmmnConst#TOTAL_COUNT")
    public static final String TOTCNT = "totcnt";
    @Deprecated(since = "20231129 @see kr.vaiv.sdt.cmmn.misc.CmmnConst#CODE")
    public static final String RESULT_CODE = "resultCode";
    @Deprecated(since = "20231129 @see kr.vaiv.sdt.cmmn.misc.CmmnConst#MESSAGE")
    public static final String RESULT_MESSAGE = "resultMessage";

    public static final String DATA = "data";
    public static final String TOTAL_COUNT = "totalCount";
    public static final String PAGE = "page";
    public static final String SIZE = "size";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
}
