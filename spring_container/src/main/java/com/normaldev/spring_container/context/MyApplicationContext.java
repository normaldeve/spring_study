package com.normaldev.spring_container.context;

import com.normaldev.spring_container.factory.MyBeanCreationException;
import com.normaldev.spring_container.stereotype.MyAutowired;
import com.normaldev.spring_container.stereotype.MyComponent;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom ApplicationContext
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 24.
 */
public class MyApplicationContext {

    private final Map<Class<?>, Object> singletonBeans = new ConcurrentHashMap<>();
    private final Set<Class<?>> componentTypes;

    public MyApplicationContext(String basePackage) {
        this.componentTypes = scanComponents(basePackage);

        // 1) 먼저 컴포넌트들을 “필요하면 만들 수 있도록” 후보만 확보
        // 2) 보통은 eager init도 가능하지만, 학습용으로는 getBean 시점에 만들도록 lazy로 가도 됨
        // 여기서는 eager로 한 번 싹 생성해두고 주입까지 끝내는 방식으로 진행
        for (Class<?> type : componentTypes) {
            getBean(type);
        }
    }

    public <T> T getBean(Class<T> requiredType) {
        Object existing = findBeanByType(requiredType);
        if (existing != null) return requiredType.cast(existing);

        // requiredType이 componentTypes에 없더라도,
        // 인터페이스/부모로 요청되면 구현체 중 @Component를 찾아 생성할 수 있게 처리
        Class<?> implType = resolveComponentType(requiredType);
        if (implType == null) {
            throw new MyBeanCreationException("No component found for type: " + requiredType.getName());
        }

        return requiredType.cast(createOrGetSingleton(implType));
    }

    private Object createOrGetSingleton(Class<?> type) {
        return singletonBeans.computeIfAbsent(type, this::createBean);
    }

    private Object createBean(Class<?> type) {
        try {
            // 1) 인스턴스 생성 (생성자 주입)
            Object instance = instantiate(type);

            // 2) 필드 주입
            injectFields(instance);

            return instance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new MyBeanCreationException("Failed to create bean: " + type.getName(), e);
        }
    }

    private Object instantiate(Class<?> type) throws Exception {
        Constructor<?> ctor = selectConstructor(type);

        ctor.setAccessible(true);
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = getBean(paramTypes[i]); // 재귀적으로 의존성 해결
        }
        return ctor.newInstance(args);
    }

    private Constructor<?> selectConstructor(Class<?> type) {
        Constructor<?>[] ctors = type.getDeclaredConstructors();

        // 1) @Autowired 붙은 생성자 우선
        for (Constructor<?> c : ctors) {
            if (c.isAnnotationPresent(MyAutowired.class)) return c;
        }

        // 2) 생성자가 1개면 그걸 사용
        if (ctors.length == 1) return ctors[0];

        // 3) 기본 생성자 사용
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new MyBeanCreationException(
                    "No suitable constructor found for " + type.getName() +
                            " (need @Autowired or single constructor or no-arg constructor)."
            );
        }
    }

    private void injectFields(Object instance) throws IllegalAccessException {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(MyAutowired.class)) continue;

            Object dependency = getBean(field.getType());
            field.setAccessible(true);
            field.set(instance, dependency);
        }
    }

    private Object findBeanByType(Class<?> type) {
        // exact match 먼저
        Object exact = singletonBeans.get(type);
        if (exact != null) return exact;

        // assignable match (인터페이스/부모 타입)
        for (Object bean : singletonBeans.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                return bean;
            }
        }
        return null;
    }

    private Class<?> resolveComponentType(Class<?> requiredType) {
        // requiredType이 @Component 클래스면 그대로 사용
        if (componentTypes.contains(requiredType)) return requiredType;

        // requiredType이 인터페이스/부모면 구현체 중 하나 선택
        List<Class<?>> candidates = new ArrayList<>();
        for (Class<?> c : componentTypes) {
            if (requiredType.isAssignableFrom(c)) candidates.add(c);
        }

        if (candidates.isEmpty()) return null;
        if (candidates.size() > 1) {
            // 스프링이면 @Primary, @Qualifier 같은 걸로 해결하지만
            // 여기서는 학습용으로 명확히 터뜨려서 원인 알게 하는게 좋음
            throw new MyBeanCreationException(
                    "Multiple components found for type: " + requiredType.getName() +
                            " -> " + candidates
            );
        }
        return candidates.get(0);
    }

    // -----------------------------
    // Component Scan (파일 시스템 기반)
    // -----------------------------
    private Set<Class<?>> scanComponents(String basePackage) {
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = cl.getResources(path);

            Set<Class<?>> result = new HashSet<>();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (!"file".equals(url.getProtocol())) continue;

                File dir = new File(url.getFile());
                findClasses(dir, basePackage, result);
            }
            // @Component만 남김
            result.removeIf(c -> !c.isAnnotationPresent(MyComponent.class));
            return result;
        } catch (Exception e) {
            throw new MyBeanCreationException("Failed to scan components for package: " + basePackage, e);
        }
    }

    private void findClasses(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException ignored) {
                    // ignore
                }
            }
        }
    }
}