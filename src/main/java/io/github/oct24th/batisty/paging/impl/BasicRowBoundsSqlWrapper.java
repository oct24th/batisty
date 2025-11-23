package io.github.oct24th.batisty.paging.impl;

import io.github.oct24th.batisty.paging.RowBoundsSqlWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 오라클(12c이상) 혹은 SqlServer 용 RowBoundsSqlWrapper 클래스
 * @see RowBoundsSqlWrapper
 */
@Lazy
@Component
public class BasicRowBoundsSqlWrapper implements RowBoundsSqlWrapper {

    @Override
    public String getTotalCountSql(String originalSql) {
        return "SELECT count(*) FROM ( " + this.removeOrderBy(originalSql) + " ) V_TOTAL_COUNT";
    }

    @Override
    public String getPagingSql(String originalSql, int offset, int limit) {
        return originalSql + " OFFSET "  + offset + " ROWS FETCH NEXT "+ limit +" ROWS ONLY";
    }
}
