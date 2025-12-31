package com.normaldeve.elasticsearchtestcontainer.repository;

import com.normaldeve.elasticsearchtestcontainer.document.Product;
import com.normaldeve.elasticsearchtestcontainer.testcontainer.ElasticSearchTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 31.
 */
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
