package io.github.oct24th.batisty.util;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class Utils {

    public static Object readObjectProperty(Object obj, String propertyName, Object defaultValue){
        try {
            Class<?> clazz = obj.getClass();

            //Method getter = clazz.isRecord() ? clazz.getMethod(propertyName)
            //        : new PropertyDescriptor(propertyName, clazz).getReadMethod();

            Method getter = new PropertyDescriptor(propertyName, clazz).getReadMethod();

            if(getter != null) return getter.invoke(obj);

            Field field;

            try {
                field = clazz.getField(propertyName);
            }catch (NoSuchFieldException e) {
                field = clazz.getDeclaredField(propertyName);
                field.setAccessible(true);
            }

            return field.get(obj);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return defaultValue;
        }
    }

    /**
     * 대상 타입의 상속 구조에서 Object 아래까지 추적하면서 모든 Field를 찾아 List로 리턴한다.
     * @param type 대상타입
     * @return 필드 리스트
     */
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        fillAllFields(fields, type);
        return fields;
    }

    public static void fillAllFields(List<Field> fields, Class<?> type) {
        if (type == null || type == Object.class) return;

        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        Utils.fillAllFields(fields, type.getSuperclass());
    }

    public static <T> Field findField(Class<T> type, String fieldName){
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> parent = type.getSuperclass();
            if(parent == Object.class) throw new RuntimeException(e);
            else return findField(parent, fieldName);
        }
    }

    public static byte[] sha256Hash(String input){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
