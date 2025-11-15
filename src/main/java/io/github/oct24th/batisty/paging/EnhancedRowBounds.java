package io.github.oct24th.batisty.paging;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.RowBounds;

public class EnhancedRowBounds extends RowBounds {

    @Getter @Setter
    private int totalCount;

    public EnhancedRowBounds(int offset, int limit) {
        super(offset, limit);
    }
}
