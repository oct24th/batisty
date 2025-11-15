package io.github.oct24th.batisty.paging;

public interface RowBoundsSqlPacker {

    String getTotalCountSql(String originalSql);

    String getPagingSql(String originalSql, int offset, int limit);
}
