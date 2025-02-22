package com.github.oct24th.batisty.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
public class DataContainer {
    @Getter private final Field field;
    @Getter private final String operator;
    @Getter private final Object value;
}
