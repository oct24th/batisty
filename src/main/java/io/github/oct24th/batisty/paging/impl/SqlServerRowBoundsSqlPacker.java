package io.github.oct24th.batisty.paging.impl;

import io.github.oct24th.batisty.paging.RowBoundsSqlPacker;

public class SqlServerRowBoundsSqlPacker implements RowBoundsSqlPacker {

    @Override
    public String getTotalCountSql(String originalSql) {
        // SQLServer는 단순 COUNT 서브쿼리로 전체 건수를 조회 가능
        return "SELECT COUNT(*) FROM (" + originalSql + ") AS __INNER_TBL";
    }


    @Override
    public String getPagingSql(String originalSql, int offset, int limit) {

        String orderBy = extractOrderBy(originalSql);
        String sqlWithoutOrderBy = removeOrderBy(originalSql);

        // ORDER BY 없으면 강제 지정 (SQLServer는 ROW_NUMBER()에도 ORDER BY 필수)
        if (orderBy == null) orderBy = "ORDER BY (SELECT NULL)";

        int end = offset + limit;

        return "SELECT __ROWNUM_TBL.* " +
               "  FROM ( " +
               "        SELECT __INNER_TBL.*, " +
               "               ROW_NUMBER() OVER (" + orderBy + ") AS ROW_NUM " +
               "          FROM ( " + sqlWithoutOrderBy + " ) AS __INNER_TBL " +
               "       ) AS __ROWNUM_TBL " +
               "WHERE ROW_NUM > " + offset +
               "  AND ROW_NUM <= " + end;
    }

    /** ORDER BY 절 추출 */
    private String extractOrderBy(String sql) {
        String lower = sql.toLowerCase();
        int idx = lower.lastIndexOf("order by");
        if (idx == -1) return null;
        return sql.substring(idx);
    }

    /** ORDER BY 절 제거 */
    private String removeOrderBy(String sql) {
        String lower = sql.toLowerCase();
        int idx = lower.lastIndexOf("order by");
        if (idx == -1) return sql;
        return sql.substring(0, idx);
    }
}