package io.github.oct24th.batisty.util;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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

    public static Class<?> getGenericType(Field field, Supplier<Class<?>> rawTypeSupplier) {

        System.out.println("field  " + field);
        Class<?> clazz = null;
        if (field.getGenericType() instanceof ParameterizedType pt) {
            Type[] actualTypeArguments = pt.getActualTypeArguments();

            if (actualTypeArguments.length > 0) {
                clazz = extractClassFromType(actualTypeArguments[0]);
            }
        }
        return clazz != null ? clazz : rawTypeSupplier.get();
    }

    /**
     * Type 객체로부터 실제 Class 객체 (Raw Type)를 추출합니다.
     * @param type 추출할 Type 객체
     * @return 추출된 Class 객체 또는 null
     */
    public static Class<?> extractClassFromType(Type type) {
        if (type == null) return null;
        if (type instanceof Class<?> clazz) return clazz;
        if (type instanceof ParameterizedType pt) return extractClassFromType(pt.getRawType());
        if (type instanceof TypeVariable<?> tv) {
            Type[] bounds = tv.getBounds();
            if (bounds.length > 0) return extractClassFromType(bounds[0]);
        }
        return null;
    }

    public static String resultMapId(Class<?> type) {
        return "BATISTY_R_MAP_" + type.getName();
    }
}
