# 쿠폰 발급 동시성 제어 프로젝트

## 📌 프로젝트 개요

선착순 쿠폰 발급 시스템에서 발생하는 **동시성 문제**를 재현하고, 다양한 해결 방법을 학습하기 위한 프로젝트입니다.

## 🎯 학습 목표

1. **동시성 문제 이해**: 멀티스레드 환경에서 발생하는 Race Condition 확인
2. **문제 재현**: 실제로 재고보다 많은 쿠폰이 발급되는 상황 체험
3. **해결 방안 학습**: 다양한 동시성 제어 기법 비교 및 적용

## 🔍 확인하려는 문제

### 시나리오
- 쿠폰 재고: 100개
- 동시 요청자: 1,000명
- 목표: 정확히 100명에게만 발급

### 예상되는 문제

```java
@Transactional
public void issue(Long userId) {
    Coupon coupon = couponRepository.findById(1L).orElseThrow();
    
    if (coupon.getStock() <= 0) {  // ① 재고 확인
        return;
    }
    
    coupon.decrease();  // ② 재고 차감
    issueRepository.save(new CouponIssue(1L, userId));  // ③ 쿠폰 발급
}
```

**문제점: Read와 Write 사이의 Race Condition**

```
시간 →
Thread A: stock 읽음 (100) → 99로 변경
Thread B: stock 읽음 (100) → 99로 변경  ← Thread A의 변경사항 무시!
Thread C: stock 읽음 (100) → 99로 변경  ← Thread A, B의 변경사항 무시!

결과: 3명이 발급받았지만 재고는 99개 (1개만 차감됨)
```

### 실제 발생하는 현상

1. **Over-issuing**: 100개보다 많은 쿠폰 발급
2. **음수 재고**: stock 값이 음수로 변경
3. **데이터 불일치**: 실제 발급 건수 ≠ 차감된 재고

## 🛠️ 기술 스택

- Java 21
- Spring Boot 4.0.1
- Spring Data JPA
- H2 Database (In-memory)
- JUnit 5

## 📊 테스트 구조

```java
@Test
void synchronized로_1000명이_동시에_요청한다() throws Exception {
    int threadCount = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    // 1000명의 사용자가 동시에 쿠폰 발급 요청
    for (int i = 0; i < threadCount; i++) {
        long userId = i;
        executor.submit(() -> {
            try {
                couponService.issue(userId);
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    
    // 결과 확인
    Coupon coupon = couponRepository.findById(1L).get();
    long issuedCount = issueRepository.count();
    
    System.out.println("남은 쿠폰 수 = " + coupon.getStock());
    System.out.println("발급된 쿠폰 수 = " + issuedCount);
}
```

## 🚀 실행 방법

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd concurrency-coupon
```

### 2. 테스트 실행
```bash
./gradlew test --tests CouponSynchronizedConcurrencyTest
```

### 3. 결과 확인
```
남은 쿠폰 수 = -50   ← 음수 발생!
발급된 쿠폰 수 = 150  ← 100개보다 많이 발급!
```

## 💡 해결 방안 (예정)

### 1. Synchronized 키워드
```java
public synchronized void issue(Long userId) {
    // 메서드 레벨 동기화
}
```
- **장점**: 구현 간단
- **단점**: 단일 서버 환경에서만 동작, @Transactional과 함께 사용 시 문제 발생

### 2. 비관적 락 (Pessimistic Lock)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.id = :id")
Coupon findByIdWithPessimisticLock(@Param("id") Long id);
```
- **장점**: 충돌이 자주 발생하는 경우 효율적
- **단점**: 성능 저하 가능성, 데드락 위험

### 3. 낙관적 락 (Optimistic Lock)
```java
@Entity
public class Coupon {
    @Version
    private Long version;
    // ...
}
```
- **장점**: 충돌이 적은 경우 효율적
- **단점**: 충돌 시 재시도 로직 필요

### 4. Named Lock
```java
@Query(value = "SELECT GET_LOCK(:key, 3000)", nativeQuery = true)
void getLock(@Param("key") String key);

@Query(value = "SELECT RELEASE_LOCK(:key)", nativeQuery = true)
void releaseLock(@Param("key") String key);
```
- **장점**: 세밀한 제어 가능
- **단점**: 구현 복잡도 증가

### 5. Redis 분산 락
```java
// Redisson 사용
RLock lock = redissonClient.getLock("coupon:1");
try {
    lock.lock();
    // 쿠폰 발급 로직
} finally {
    lock.unlock();
}
```
- **장점**: 분산 환경에서 동작, 확장성 우수
- **단점**: Redis 인프라 필요, 구현 복잡도 높음

### 6. Redis + 메시지 큐
```
User → API → Redis (재고 차감) → Message Queue → Worker (DB 저장)
```
- **장점**: 최고의 성능, 대용량 트래픽 처리 가능
- **단점**: 아키텍처 복잡도 높음, 일관성 보장 어려움

## 📈 성능 비교 (예정)

| 방식 | TPS | 응답시간 | 확장성 | 복잡도 |
|------|-----|---------|--------|--------|
| Synchronized | 낮음 | 보통 | ❌ | ⭐ |
| 비관적 락 | 보통 | 느림 | ⚠️ | ⭐⭐ |
| 낙관적 락 | 높음 | 빠름 | ⚠️ | ⭐⭐⭐ |
| Named Lock | 보통 | 보통 | ⚠️ | ⭐⭐⭐ |
| Redis 분산 락 | 높음 | 빠름 | ✅ | ⭐⭐⭐⭐ |
| Redis + MQ | 매우 높음 | 매우 빠름 | ✅ | ⭐⭐⭐⭐⭐ |

## 🎓 학습 포인트

1. **동시성 문제의 본질 이해**
    - Race Condition이 발생하는 이유
    - Read-Modify-Write 패턴의 위험성

2. **트랜잭션 격리 수준의 한계**
    - @Transactional만으로는 해결 불가능
    - DB 격리 수준(REPEATABLE READ, SERIALIZABLE)의 이해

3. **적절한 해결 방안 선택**
    - 트래픽 규모
    - 인프라 환경 (단일/분산)
    - 비즈니스 요구사항 (정합성 vs 성능)