package com.github.oct24th.batisty.proxy;

/**
 * BatistyDAO를 이용해서 SELECT, DELETE, UPDATE할 경우 where절 생성을 위한 proxy 생성용 interface<br>
 */
public interface WhereCauseProxy<T> {
    T equal();
    T not();
    T notLike();
    T like();
    T lt();
    T lte();
    T gt();
    T gte();
}
