package io.github.oct24th.batisty.paging;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PagingResult<T> {

    /** 총 데이터 건수 */
    private int totalCount;
    /** 마지막 페이지 번호 */
    private int lastPageNo;
    /** 스킵된 행의 수(첫데이터의 행번호-1) */
    private int rowOffset;
    /** 실제 페이지 데이터 */
    private List<T> data;
}
