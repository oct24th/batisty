package com.github.oct24th.batisty.proxy;

import java.util.function.Consumer;

/**
 * BatistyDAO를 이용해서 UPDATE할 경우 대상 객체의 proxy 생성용 interface<br>
 * update문은 데이터 바인딩 부분이 set절과 where의 구분이 필요하기 때문에 사용한다.
 */
public interface UpdateEntityProxy<T> {
    void set(Consumer<T> consumer);
    void where(Consumer<WhereCauseProxy<T>> consumer);
}
