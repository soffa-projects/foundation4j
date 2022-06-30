package dev.soffa.foundation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {

    private boolean hasMore;
    private long totalElements;
    private int totalPages;
    private int itemsPerPage;
    private int page;
    private int count;

}
