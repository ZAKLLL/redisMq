package com.zakl.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jws.Oneway;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 部分反射常用工具
 */
@SuppressWarnings(value = {"unchecked", "all"})
@Slf4j
public class ReflectionUtils {

    /**
     * 获取拥有某注解的字段
     *
     * @param o
     * @param annotationType
     * @return
     */
    public static Field getFieldWithAnnotation(Object o, Class<?> annotationType) {
        return getFieldWithAnnotation(o.getClass(), annotationType);
    }

    public static <T> Field getFieldWithAnnotation(Class<T> tClass, Class<?> annotationType) {
        for (Field field : tClass.getDeclaredFields()) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotationType.isAssignableFrom(annotation.annotationType())) {
                    return field;
                }
            }
        }
        throw new RuntimeException(tClass.getName() + "不存在该注解" + annotationType.getName());
    }


    /**
     * 获取指定类型的字段名称
     *
     * @param tClass
     * @param targetTypes
     * @return
     */
    public static String[] getFieldNamesInTargetTypes(Class tClass, Class... targetTypes) {
        List<String> ret = new ArrayList<>();
        for (Field field : tClass.getDeclaredFields()) {
            for (Class targetType : targetTypes) {
                if (field.getType().isAssignableFrom(targetType)) {
                    ret.add(field.getName());
                    continue;
                }
            }
        }
        return ret.toArray(new String[0]);
    }

    /**
     * 获取对应对象的某个字段的具体值
     *
     * @param object
     * @param fieldName
     * @param fieldClass
     * @param <T>
     * @return
     * @throws IllegalAccessException
     */
    @SneakyThrows
    public static <T> T getFieldValueWithFieldName(Object object, String fieldName, Class<T> fieldClass) throws IllegalAccessException {
        Class<?> c = object.getClass();
        Field field = c.getDeclaredField(fieldName);
        //同名,同类型
        if ((field.getName().equals(fieldName)) && field.getType().isAssignableFrom(fieldClass)) {
            field.setAccessible(true);
            return fieldClass.cast(field.get(object));
        }
        return null;
    }


    /**
     * 反射执行target类中的方法
     *
     * @param target
     * @param methodName
     * @param args
     * @return
     */
    public static Object reflectInvoke(Object target, String methodName, Object... args) throws Throwable {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new RuntimeException("not support Null param!");
            }
            argTypes[i] = args[i].getClass();
        }
        return reflectInvoke(target, methodName, args, argTypes);
    }


    /**
     * 反射执行target类中的方法
     *
     * @param target
     * @param methodName
     * @param args
     * @param argTypes
     * @return
     */
    public static Object reflectInvoke(Object target, String methodName, Object[] args, Class<?>[] argTypes) throws Throwable {
        try {
            Method method;
            if (target instanceof Class) {
                //静态方法调用
                method = ((Class) target).getDeclaredMethod(methodName, argTypes);
            } else {
                method = target.getClass().getDeclaredMethod(methodName, argTypes);
            }
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                return null;
            } else if (e instanceof InvocationTargetException) {
                throw ((InvocationTargetException) e).getTargetException();
            }
            e.printStackTrace();
            throw e;
        }
    }

    @SneakyThrows
    public static Object reflectInvoke(Object target, Method method, Object[] args) {
        method.setAccessible(true);
        if (Modifier.isStatic(method.getModifiers())) {
            return method.invoke(target.getClass(), args);
        } else {
            return method.invoke(target, args);
        }
    }
}

