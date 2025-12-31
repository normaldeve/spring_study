# 들어가며

이전 프로젝트에서 Elasticsearch를 이용한 검색 서비스를 구현하였고 해당 기능에 대한 단위 테스트 및 통합 테스트 작성 중 문제가 발생했습니다.

개발 환경에서 Docker로 새로운 Elasticsearch를 띄워야 한다는 점과 **CI 테스트**에서는 테스트가 통과하지 않는다는 점이었습니다.

이것을 해결하기 위해 `TestContainer`라는 것을 알게 되었고, 위 문서는 해당 설정을 통해 간단한 CRUD 통합 테스트 과정을 담았습니다.

# TestContainer

`TestContainer`는 통합 테스트 지원을 위해 개발된 Open Source Java Library로 Docker Container를 이용하여 테스트 환경 구축을 간편하게 합니다.

따라서 개발 환경에서 `Mock`을 사용한 단위 테스트 뿐만 아니라 실제 `Elasticsearch`를 사용하여 통합 테스트 코드를 작성 및 실행할 수 있는 환경을 구축할 수 있습니다.

`TestContainer`를 사용하면 다음과 같은 이점이 존재합니다.

- `Local`에 직접 Elasticsearch를 구동하지 않아도 테스트 실행 시 자동으로 구동 및 제거가 됩니다.
- 각 테스트 시나리오마다 `동일한 테스트 환경`을 보장할 수 있습니다.
- `CI/CD`에서 매번 테스트를 진행하게 되는데, 연동에 매우 용이합니다.

## 목표
1. Product Document CRUD 코드를 테스트 컨테이너를 통해 통합 테스트를 진행합니다.
2. CI 파이프라인에서 해당 테스트 코드가 통과함을 확인합니다.

## TestContainer 설정
```java
@Testcontainers
@TestConfiguration
@EnableElasticsearchRepositories(basePackageClasses = ProductRepository.class)
public class ElasticSearchTestContainer {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.6.0";

    @Container
    private static final ElasticsearchContainer container = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("xpack.security.http.ssl.enabled", "false")
            .withCommand("sh", "-c", "elasticsearch-plugin install analysis-nori && elasticsearch");

    static {
        container.start();
    }

    @DynamicPropertySource
    static void setElasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", container::getHttpHostAddress);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create(container.getHttpHostAddress())).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        return new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
    }
}
```
- `@TestConainers` 어노테이션으로 테스트 컨테이너 관리가 가능합니다.
- `withEnv()` 설정으로 보안 및 각종 설정이 가능합니다.
- `withCommand()`로 Nori 형태소 분석기 설치가 가능합니다.
- `RestClient`, `ElasticsearchClient`을 Bean으로 등록합니다.

## 테스트 코드 실행
```java
@SpringBootTest
@Import({ElasticSearchTestContainer.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void 상품을_저장하고_ID로_조회할_수_있다() {
        // given
        Product product = Product.builder()
                .id("product-1")
                .name("원목 침대")
                .price(BigDecimal.valueOf(350_000))
                .build();

        // when
        productRepository.save(product);

        // then
        Optional<Product> result = productRepository.findById("product-1");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("원목 침대");
        assertThat(result.get().getPrice()).isEqualByComparingTo("350000");
    }

    @Test
    void 상품_가격을_수정할_수_있다() {
        // given
        Product product = Product.builder()
                .id("product-2")
                .name("패브릭 소파")
                .price(BigDecimal.valueOf(500_000))
                .build();

        productRepository.save(product);

        // when
        Product updatedProduct = Product.builder()
                .id("product-2")
                .name("패브릭 소파")
                .price(BigDecimal.valueOf(450_000))
                .build();

        productRepository.save(updatedProduct);

        // then
        Product result = productRepository.findById("product-2").orElseThrow();

        assertThat(result.getPrice()).isEqualByComparingTo("450000");
    }

    @Test
    void 상품을_삭제할_수_있다() {
        // given
        Product product = Product.builder()
                .id("product-3")
                .name("식탁 의자")
                .price(BigDecimal.valueOf(120_000))
                .build();

        productRepository.save(product);

        // when
        productRepository.deleteById("product-3");

        // then
        Optional<Product> result = productRepository.findById("product-3");
        assertThat(result).isEmpty();
    }

    @Test
    void 전체_상품을_조회할_수_있다() {
        // given
        productRepository.save(Product.builder()
                .id("product-4")
                .name("책상")
                .price(BigDecimal.valueOf(200_000))
                .build());

        productRepository.save(Product.builder()
                .id("product-5")
                .name("책장")
                .price(BigDecimal.valueOf(180_000))
                .build());

        // when
        List<Product> products = StreamSupport
                .stream(productRepository.findAll().spliterator(), false)
                .toList();

        // then
        assertThat(products).hasSize(2);
    }
}
```

### Docker Desktop
<img width="500" height="135" alt="image" src="https://github.com/user-attachments/assets/f5efd7d3-0c8a-4c1b-b807-8e4c6a4a975b" />
<img width="500" height="240" alt="image" src="https://github.com/user-attachments/assets/ffd938b7-5f41-4dd4-b41f-e5f7ed56f56b" />

테스트가 실행되면 1번째 사진처럼 TestContainer가 등록 및 실행되는 것을 확인할 수 있습니다.

이후 테스트가 종료되면 2번째 사진처럼 해당 컨테이너가 삭제되는 것을 확인할 수 있었습니다.

### Result
<img width="550" height="340" alt="image" src="https://github.com/user-attachments/assets/81da8083-e026-46b2-be6b-7e851d268083" />

테스트 결과도 통과함을 확인하였습니다.

## CI Pipeline
이제 CI 환경에서도 TestContainer가 동작하는지 확인하였습니다.

<img width="900" height="400" alt="image" src="https://github.com/user-attachments/assets/e8ac3162-a301-42f3-8adc-1f5fb29d0325" />

CI 환경에서도 정상적으로 테스트가 성공했음을 확인하였습니다.

# 마무리
TestContainer를 사용하여 개발 환경과 CI 환경에서 테스트 코드를 통과하는 것을 확인하였습니다.

하지만 장점만 있는 것은 아닙니다.

바로 테스트 속도가 매우 느려진다는 단점이 존재하는데요, 이는 테스트 컨테이너를 재사용하는 등 여러 방법을 통해 해결 방법을 찾아야 합니다.