package com.ecommerce.project.specification;

import com.ecommerce.project.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {
    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> hasCategory(Integer categoryId) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> categories = root.join("categories", JoinType.INNER);
            return cb.equal(categories.get("id"), categoryId);
        };
    }
}
