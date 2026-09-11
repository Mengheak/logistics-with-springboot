package com.chheang.mengheak.logisticsapis.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonPropertyOrder({"data", "pagination"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {

    private List<T> data;
    private PaginationMetadata pagination;

    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(page.getContent(), metadataOf(page, null));
    }

    public static <T> PaginatedResponse<T> from(Page<T> page, String baseUrl) {
        Link links = generateLinks(baseUrl, page.getNumber(), page.getSize(), page.getTotalPages());
        return new PaginatedResponse<>(page.getContent(), metadataOf(page, links));
    }

    private static PaginationMetadata metadataOf(Page<?> page, Link links) {
        return new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious(),
                links
        );
    }

    private static Link generateLinks(String baseUrl, int page, int size, int totalPages) {
        Link links = new Link();
        links.setSelf(buildUrl(baseUrl, page, size));
        links.setFirst(buildUrl(baseUrl, 0, size));
        links.setLast(buildUrl(baseUrl, Math.max(totalPages - 1, 0), size));
        if (page > 0) {
            links.setPrevious(buildUrl(baseUrl, page - 1, size));
        }
        if (page < totalPages - 1) {
            links.setNext(buildUrl(baseUrl, page + 1, size));
        }
        return links;
    }

    private static String buildUrl(String baseUrl, int page, int size) {
        return new StringBuilder(baseUrl)
                .append("?page=").append(page)
                .append("&size=").append(size)
                .toString();
    }
}
