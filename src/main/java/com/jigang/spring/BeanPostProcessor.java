package com.jigang.spring;

public interface BeanPostProcessor {
    //bean初始化方法调用前被调用
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    //bean初始化方法调用后被调用
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
