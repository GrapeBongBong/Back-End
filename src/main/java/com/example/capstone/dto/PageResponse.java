package com.example.capstone.dto;

import com.example.capstone.entity.ExchangePost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber; //페이지 번호
    private int pageSize; //페이지 크기
    private long totalElements; //전체 개수
    private int totalPages; //전체 페이지
    private boolean hasNextPage; //다음 페이지
    private boolean hasPreviousPage; //이전 페이지

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean hasNextPage, boolean hasPreviousPage) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNextPage = hasNextPage;
        this.hasPreviousPage = hasPreviousPage;
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        List<T> content = page.getContent();
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();
        boolean hasNextPage = page.hasNext();
        boolean hasPreviousPage = page.hasPrevious();
        return new PageResponse<>(content, pageNumber, pageSize, totalElements, totalPages, hasNextPage, hasPreviousPage);
    }

}
