package io.github.oct24th.batisty.paging;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 페이징 처리에 사용되는 람다식으로부터 클래스이름 및 메소드 이름을 추출하기 위한 FunctionalInterface
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
