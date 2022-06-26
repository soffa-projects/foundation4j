package dev.soffa.foundation.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {

    public static Paging DEFAULT = new Paging(1, 50);
    public static int DEFAULT_MAX_SIZE = 1000;
    @Schema(defaultValue = "1", nullable = true)
    @Parameter(in= ParameterIn.QUERY)
    private int page;

    @Schema(defaultValue = "50", nullable = true)
    private int size = 50;

    @Schema(nullable = true)
    private String sort;

    public Paging(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        this.page = page;
        this.size = size;
    }


    public void setPage(int page) {
        if (page < 1) {
            page = 1;
        }
        this.page = page;
    }

    public Paging cap() {
        if (size > DEFAULT_MAX_SIZE) {
            size = DEFAULT_MAX_SIZE;
        }
        return this;
    }

    public static Paging of(Paging paging) {
        return of(paging, "id");
    }

    public static Paging of(Paging paging, String defautlSort) {
        Paging p = paging;
        if (p == null) {
            p = new Paging(DEFAULT.page, DEFAULT.size);
        }
        if (p.sort == null) {
            p.sort = defautlSort;
        }
        if (p.page < 1) {
            p.page = 1;
        }
        return p;
    }
}
