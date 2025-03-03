package io.github.oct24th.batisty.proxy;

import io.github.oct24th.batisty.common.DataStore;

import java.util.List;

/**
 * BatistyDAO를 이용해서 쿼리할 경우 대상 객체의 기본 proxy 생성용 interface
 */
public interface BasicEntityProxy extends StatementIdSupplier{
    List<DataStore> getDataStores();
}
