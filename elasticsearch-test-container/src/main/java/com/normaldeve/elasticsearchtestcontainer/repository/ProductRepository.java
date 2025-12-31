package com.normaldeve.elasticsearchtestcontainer.repository;

import com.normaldeve.elasticsearchtestcontainer.document.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Product Elasticsearch Repository
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 31.
 */
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

}
