package io.github.oct24th.batisty.result.impl;

import io.github.oct24th.batisty.result.MapValueTypeHandler;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Clob;

@Component
public class ClobToStringMapValueTypeHandler implements MapValueTypeHandler {
    @Override
    public boolean isSupport(PropertyTokenizer prop, Object value) {
        return value instanceof Clob;
    }

    @Override
    public Object convert(Object value) {
        Clob clob = (Clob) value;

        if (clob == null) return null;

        try {
            int bufferSize = 4096 * 8; // 32KB
            long len = clob.length();

            // 버퍼크기 이하라면 substring이 더 빠르고 안전
            if (len <= bufferSize) return clob.getSubString(1, (int) len);

            // 버퍼크기 초과라면 StringBuilder 의 사이즈를 미리 지정해서
            // 메모리 재할당으로 인한 오버헤드가 발생하지 않도록 하고 버퍼 사이즈단위로 read
            StringBuilder sb = new StringBuilder((int) len);

            try (Reader reader = clob.getCharacterStream();
                 BufferedReader br = new BufferedReader(reader, bufferSize)) {

                char[] buffer = new char[bufferSize];
                int read;
                while ((read = br.read(buffer)) != -1) {
                    sb.append(buffer, 0, read);
                }
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("CLOB convert failed.", e);
        } finally {
            try {
                clob.free();
            } catch (Exception ignored) {}
        }
    }
}
