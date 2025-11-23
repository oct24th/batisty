package io.github.oct24th.batisty.annotation;

import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface UseGeneratedKeys {
}
