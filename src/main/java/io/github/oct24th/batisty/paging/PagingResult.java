package io.github.oct24th.batisty.paging;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagingResult<T> {

    private int totalCount;
    private int lastPageNo;
    private List<T> data;
}
