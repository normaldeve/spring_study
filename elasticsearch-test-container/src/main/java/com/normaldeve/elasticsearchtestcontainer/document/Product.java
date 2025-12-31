package com.normaldeve.elasticsearchtestcontainer.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;

/**
 * Product Document
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 31.
 */
@Document(indexName = "products")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    private String name;

    private BigDecimal price;
}

