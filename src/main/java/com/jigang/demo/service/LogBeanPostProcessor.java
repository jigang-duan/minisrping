package com.jigang.demo.service;

import com.jigang.spring.BeanPostProcessor;
import com.jigang.spring.Component;

@Component
public class LogBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("postProcessBeforeInitialization..."+beanName+"=>"+bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("postProcessAfterInitialization..."+beanName+"=>"+bean);
        return bean;
    }
}
