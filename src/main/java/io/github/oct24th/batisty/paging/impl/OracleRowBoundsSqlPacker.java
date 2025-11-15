package io.github.oct24th.batisty.paging.impl;

import io.github.oct24th.batisty.paging.RowBoundsSqlPacker;

public class OracleRowBoundsSqlPacker implements RowBoundsSqlPacker {

    @Override
    public String getTotalCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM ( " + originalSql + " )";
    }

    @Override
    public String getPagingSql(String originalSql, int offset, int limit) {
        return "SELECT *" +
               "  FROM ( SELECT __INNER_TBL.*" +
               "              , ROWNUM AS ROW_NUM" +
               "           FROM ( " + originalSql + " ) __INNER_TBL" + " ) " +
               " WHERE ROW_NUM >  " + offset +
               "   AND ROW_NUM <= " + (offset + limit);
    }
}
