package com.jigang.demo;

import com.jigang.demo.service.UserService;
import com.jigang.spring.MiniApplicationContext;

public class Test {

    public static void main(String[] args) {

        // 启动Spring
        MiniApplicationContext applicationContext = new MiniApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}
