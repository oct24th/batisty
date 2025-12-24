package io.github.oct24th.batisty.proxy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.oct24th.batisty.common.DataStore;

import java.util.List;

/**
 * BatistyDAO를 이용해서 쿼리할 경우 대상 객체의 기본 proxy 생성용 interface
 * 프록세객체가 직렬화되는경우가 생길때 getDataStores를 무시하도록 JsonIgnore처리
 */
public interface BasicEntityProxy extends StatementIdSupplier{
    @JsonIgnore
    List<DataStore> getDataStores(); 
}
