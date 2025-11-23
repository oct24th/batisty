package io.github.oct24th.batisty.paging;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.RowBounds;

/**
 * 페이징 처리를 위한 RowBounds의 확장 클래스
 * PreparedStatementInterceptor 가 독작하는 기준이 된다.
 */
@Getter
public class EnhancedRowBounds extends RowBounds {

    @Setter
    private int totalCount;
    private final int rowOffset;
    private final int rowCountPerPage;

    public EnhancedRowBounds(int offset, int limit) {
        super(offset, limit);
        this.rowOffset = offset;
        this.rowCountPerPage = limit;
    }

    public int getLastPageNo() {
        return (int) Math.ceil((double) totalCount / rowCountPerPage);
    }
}
