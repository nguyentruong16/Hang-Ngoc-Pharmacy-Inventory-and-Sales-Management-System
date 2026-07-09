package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Minimal (id, name) projection of a {@link com.example.project.entity.Product}, used for the
 * product picker on the Purchase Invoice create page. Deliberately not the raw {@code Product}
 * entity: that page embeds the product list into inline JavaScript via
 * {@code th:inline="javascript"}, and Thymeleaf's Jackson mapper there serializes every field —
 * including lazy {@code @ManyToOne} relations like {@code typeID}/{@code producerID}. Whenever one
 * of those is actually set (a real {@code Type}/{@code Producer} assigned), Jackson tries to
 * serialize the uninitialized Hibernate proxy and fails with
 * {@code InvalidDefinitionException: No serializer found for ... ByteBuddyInterceptor}. This
 * projection only carries the two fields the picker actually needs, so the proxy is never touched.
 */
@Getter
@AllArgsConstructor
public class ProductOptionResponse {

    private Integer productID;

    private String name;
}
