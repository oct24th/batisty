package io.github.oct24th.batisty.paging.impl;

import io.github.oct24th.batisty.paging.RowBoundsSqlWrapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 오라클(12c이상) 혹은 SqlServer 용 RowBoundsSqlWrapper 클래스
 * 만약 다른 종류 혹은 버전의 DB라면 RowBoundsSqlWrapper를 별도로 구현해서
 * @Primary @Component를 사용해서 spring bean으로 등록해주면 이 클래스는 로드되지 않는다.
 * @see RowBoundsSqlWrapper
 */
@Lazy
@Component
public class BasicRowBoundsSqlWrapper implements RowBoundsSqlWrapper {

    @Override
    public String getTotalCountSql(String originalSql) {
        return """
               SELECT count(*)
                 FROM ( %s ) V_TOTAL_COUNT
               """.stripIndent().formatted(this.removeOrderBy(originalSql));
    }

    @Override
    public String getPagingSql(String originalSql, int offset, int limit) {
        return """
               %s
               OFFSET %d ROWS
               FETCH NEXT %d ROWS ONLY;
               """.stripIndent().formatted(originalSql, offset, limit);
    }
}
