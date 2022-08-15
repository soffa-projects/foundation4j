package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedList<T> {

    private List<T> data;
    private Pagination paging;

    public static <T> PagedList<T> of(List<T> items, long total, Paging paging) {
        boolean hasMore = total > ((long) paging.getPage() * paging.getSize());
        int pages = (int) (total / paging.getSize());
        if (total % paging.getSize() > 0) {
            pages++;
        }
        List<T> elements = items;
        if (elements == null) {
            elements = new ArrayList<>();
        }
        Pagination p = new Pagination(hasMore, total, pages, paging.getSize(), paging.getPage(), elements.size());
        return new PagedList<>(elements, p);
    }


}
