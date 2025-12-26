# 🌱 Custom Spring IoC Container

Spring Framework의 핵심 기능인 IoC(Inversion of Control) Container를 직접 구현하여 Spring의 동작 원리를 깊이 있게 학습하는 프로젝트입니다.

## 📚 프로젝트 개요

Spring Framework가 어떻게 객체의 생성과 의존성을 관리하는지 이해하기 위해, 핵심 기능들을 직접 구현했습니다.

### 구현된 주요 기능

-  **컴포넌트 스캔** (Component Scan)
-  **의존성 주입** (Dependency Injection)
    - 생성자 주입 (Constructor Injection)
    - 필드 주입 (Field Injection)
-  **싱글톤 관리** (Singleton Scope)
-  **인터페이스 기반 의존성 해결**
- **다중 후보 빈 감지**

## 🏗️ 프로젝트 구조

```
spring_container/
├── src/main/java/com/normaldeve/spring_container/
│   ├── beans/factory/
│   │   └── MyBeanCreationException.java      # 커스텀 예외
│   ├── context/
│   │   └── MyApplicationContext.java          # 핵심 IoC 컨테이너
│   ├── stereotype/
│   │   ├── MyComponent.java                   # @Component 역할
│   │   └── MyAutowired.java                   # @Autowired 역할
│   └── example/
│       ├── Main.java                          # 실행 예제
│       ├── TestRepository.java
│       ├── TestService.java                   # 필드 주입 예제
│       └── TestService2.java                  # 생성자 주입 예제
└── src/test/java/
    └── MyApplicationContextTest.java          # 통합 테스트
```

## 🔍 핵심 구현 상세

### 1. 커스텀 어노테이션

#### @MyComponent
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyComponent {
}
```
Spring의 `@Component`를 모방한 어노테이션으로, 이 어노테이션이 붙은 클래스는 IoC 컨테이너가 관리하는 빈으로 등록됩니다.

#### @MyAutowired
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MyAutowired {
}
```
Spring의 `@Autowired`를 모방한 어노테이션으로, 의존성 주입이 필요한 필드에 사용됩니다.

### 2. MyApplicationContext

IoC 컨테이너의 핵심 클래스입니다.

#### 주요 기능

**컴포넌트 스캔**
```java
public MyApplicationContext(String basePackage) {
    this.componentTypes = scanComponents(basePackage);
    // 스캔된 컴포넌트들을 eager 초기화
    for (Class<?> type : componentTypes) {
        getBean(type);
    }
}
```
- 지정된 패키지를 재귀적으로 탐색
- `@MyComponent`가 붙은 클래스를 찾아 등록

**빈 생성 및 관리**
```java
private final Map<Class<?>, Object> singletonBeans = new ConcurrentHashMap<>();

public <T> T getBean(Class<T> requiredType) {
    // 싱글톤 캐시 확인
    // 없으면 생성 및 의존성 주입
    // 타입 매칭 (인터페이스 지원)
}
```
- `ConcurrentHashMap`으로 thread-safe한 싱글톤 관리
- 타입 기반 빈 조회 지원
- 인터페이스로 조회 시 구현체 자동 매칭

**생성자 주입**
```java
private Constructor<?> selectConstructor(Class<?> type) {
    // 1. @MyAutowired가 붙은 생성자 우선
    // 2. 생성자가 1개면 그것을 사용 (Spring 4.3+ 동작)
    // 3. 기본 생성자 사용
}
```
- 생성자 파라미터의 의존성을 재귀적으로 해결
- Spring의 생성자 선택 로직을 동일하게 구현

**필드 주입**
```java
private void injectFields(Object instance) throws IllegalAccessException {
    for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(MyAutowired.class)) {
            Object dependency = getBean(field.getType());
            field.setAccessible(true);
            field.set(instance, dependency);
        }
    }
}
```
- Reflection을 사용하여 private 필드에도 주입 가능


### 인터페이스 기반 의존성

```java
public interface Repository {
    List<String> findAll();
}

@MyComponent
public class TestRepository implements Repository {
    @Override
    public List<String> findAll() {
        return Arrays.asList("Data1", "Data2");
    }
}

@MyComponent
public class Service {
    @MyAutowired
    private Repository repository;  // 인터페이스로 주입
}
```

## 🔧 개선 사항 및 제한사항

### 현재 제한사항

1. **순환 참조 미처리**
    - 순환 참조 발생 시 `StackOverflowError`
    - Spring은 `BeanCurrentlyInCreationException` 발생

2. **@MyAutowired의 제한된 Target**
    - 현재: `ElementType.FIELD`만 지원
    - 개선 필요: `ElementType.CONSTRUCTOR` 추가

3. **프로토타입 스코프 미지원**
    - 현재는 싱글톤만 지원
    - 매번 새로운 인스턴스가 필요한 경우 처리 불가

### 향후 개선 계획

- [ ] 순환 참조 감지 로직 추가
- [ ] `@Primary`, `@Qualifier` 지원
- [ ] 프로토타입 스코프 지원
- [ ] Lazy 초기화 옵션
- [ ] 생명주기 콜백 (`@PostConstruct`, `@PreDestroy`)
- [ ] AOP(Aspect-Oriented Programming) 지원

## 🎯 학습 포인트

### 1. Reflection API 활용
```java
// 클래스 정보 조회
Class<?> clazz = Class.forName(className);

// 생성자로 인스턴스 생성
Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes);
Object instance = ctor.newInstance(args);

// 필드 값 설정
Field field = clazz.getDeclaredField("fieldName");
field.setAccessible(true);
field.set(instance, value);
```

### 2. 의존성 주입 패턴
- **생성자 주입**: 불변성 보장, 테스트 용이
- **필드 주입**: 간결하지만 테스트 어려움
- **세터 주입**: 선택적 의존성에 사용 (미구현)

### 3. 싱글톤 패턴의 한계
- 멀티스레드 환경에서의 동시성 문제
- `ConcurrentHashMap`으로 thread-safe 보장

### 4. 타입 기반 빈 검색
```java
// exact match
Object exact = singletonBeans.get(type);

// assignable match (다형성)
for (Object bean : singletonBeans.values()) {
    if (type.isAssignableFrom(bean.getClass())) {
        return bean;
    }
}
```