package com.fkp.util;

import com.fkp.domain.User;

import java.lang.reflect.Field;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2023/8/8 14:19
 */
public class ReflectionUtils {

    public static void setFieldValue(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = target.getClass().getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(target, value);
        declaredField.setAccessible(false);
    }
}
