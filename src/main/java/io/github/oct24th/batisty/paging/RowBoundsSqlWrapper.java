package io.github.oct24th.batisty.paging;

/**
 * 페이징 쿼리 Wrapping 처리를 위한 인터페이스
 * 이 인터페이스를 DB 벤더에 따라 적절히 구현해서 사용   
 */
public interface RowBoundsSqlWrapper {

    /**
     * 토탈카운트 처리용  SQL을 만들어준다.
     * @param originalSql 원본 쿼리
     * @return 토탈카운트 쿼리
     */
    String getTotalCountSql(String originalSql);

    /**
     * 페이징 처리용  SQL을 만들어준다.
     * @param originalSql 원본 쿼리
     * @return 페이징 쿼리
     */
    String getPagingSql(String originalSql, int offset, int limit);

    /**
     * 토탈카운트를 구하기위해 쿼리를 감쌀때는 정렬이 필요없다(속도향상)
     * @param originalSql 원본 쿼리
     * @return 마지막 order by가 제거된 쿼리
     */
    default String removeOrderBy(String originalSql) {
        return originalSql.replaceAll("(?i)ORDER\\s+(SIBLINGS\\s+)?BY[\\s\\S]*$", "");
    }
}
