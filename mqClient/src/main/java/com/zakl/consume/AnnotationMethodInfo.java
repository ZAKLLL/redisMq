package com.zakl.consume;

import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * @author ZhangJiaKui
 * @classname AnnotationMethodInfo
 * @description TODO
 * @date 6/4/2021 9:44 AM
 */
@Data
@ToString
public class AnnotationMethodInfo<T> {
    T annotation;
    Method method;

    public AnnotationMethodInfo(T annotation, Method method) {
        this.annotation = annotation;
        this.method = method;
    }

    Object targetObject;
}
