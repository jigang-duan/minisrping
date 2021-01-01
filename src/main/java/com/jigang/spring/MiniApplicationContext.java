package com.jigang.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MiniApplicationContext {

    private final Class<?> configClass;

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final List<BeanPostProcessor> processorList = new ArrayList<>();

    public MiniApplicationContext(Class<?> configClass) {
        this.configClass = configClass;

        // 扫描，得到class
        List<Class<?>> classList = scan(configClass);

        // 过滤 BeanDefinition
        filterBeanDefinition(classList);

        // 基于class创建单例Bean
        instanceSingletonBean();
    }

    private void filterBeanDefinition(List<Class<?>> classList) {
        for (Class<?> clazz: classList) {
            if (clazz.isAnnotationPresent(Component.class)) {
                if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                    BeanPostProcessor instance = createBeanPostProcessor(clazz);
                    if (instance != null) {
                        processorList.add(instance);
                    }
                }

                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                Component componentAnnotation = clazz.getAnnotation(Component.class);
                String beanName = componentAnnotation.value();

                if (clazz.isAnnotationPresent(Scope.class)) {
                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                    beanDefinition.setScope(scopeAnnotation.value());
                } else {
                    beanDefinition.setScope("singleton");
                }

                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }
    }

    private BeanPostProcessor createBeanPostProcessor(Class<?> clazz) {
        try {
            return  (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void instanceSingletonBean() {
        for (Map.Entry<String, BeanDefinition> entry: beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                if (!singletonObjects.containsKey(beanName)) {
                    Object bean = doCreateBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
            }
        }
    }

    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        try {
            // 1. 实例化
            Object bean = beanClass.getDeclaredConstructor().newInstance();

            // 2. 属性填充
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)) {

                    Object o = getBean(field.getName());
                    field.setAccessible(true);
                    field.set(bean, o);
                }
            }

            // 3. Aware
            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            // 初始化前
            for (BeanPostProcessor processor: processorList) {
                processor.postProcessBeforeInitialization(bean, beanName);
            }

            // 4. 初始化
            if (bean instanceof InitializingBean) {
                ((InitializingBean) bean).afterProperties();
            }

            // 初始化后
            for (BeanPostProcessor processor : processorList) {
                processor.postProcessAfterInitialization(bean, beanName);
            }

            return bean;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Class<?>> scan(Class configClass) {
        List<Class<?>> classList = new ArrayList<>();

        if (!configClass.isAnnotationPresent(ComponentScan.class)) {
            throw new NoClassDefFoundError("不能找到ComponentScan");
        }
        ComponentScan csAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = csAnnotation.value(); // 包路径

        path = path.replace(".", "/");

        ClassLoader classLoader = MiniApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f: files) {
                String classPath = f.getAbsolutePath();
                classPath = classPath.substring(classPath.indexOf("com"), classPath.indexOf(".class"));
                classPath = classPath.replace("/", ".");
                try {
                    Class<?> clazz = classLoader.loadClass(classPath);
                    classList.add(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classList;
    }

    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            return null;
        }
        if ("prototype".equals(beanDefinition.getScope())) {
            return doCreateBean(beanName, beanDefinition);
        } else if ("singleton".equals(beanDefinition.getScope())) {
            Object bean = singletonObjects.get(beanName);
            if (bean == null) {
                return doCreateBean(beanName, beanDefinition);
            }
            return bean;
        }
        return null;
    }
}
