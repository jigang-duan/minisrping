package com.jigang.demo.service;

import com.jigang.spring.*;

@Component("userService")
@Lazy
@Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean {

    @Autowired
    private User user;

    private String beanName;

    public void test() {
        System.out.println(String.format("I am %s", beanName));
        System.out.println(user);
        user.test();
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterProperties() {
        System.out.println("afterProperties");
    }
}
