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

    public static final int BASE_INDEX = 1;
    private static final int DEFAULT_FETCH_SIZE = 50;
    public static final Paging DEFAULT = new Paging(BASE_INDEX, DEFAULT_FETCH_SIZE);
    public static final int DEFAULT_MAX_SIZE = 1000;
    @Schema(defaultValue = "1", nullable = true)
    @Parameter(in= ParameterIn.QUERY)
    private int page;

    @Schema(defaultValue = "50", nullable = true)
    private int size = DEFAULT_FETCH_SIZE;

    @Schema(nullable = true)
    private String sort;

    public Paging(int page, int size) {
        if (page < BASE_INDEX) {
            page = BASE_INDEX;
        }
        this.page = page;
        this.size = size;
    }

    public Paging cap() {
        if (size > DEFAULT_MAX_SIZE) {
            size = DEFAULT_MAX_SIZE;
        }
        if (size < BASE_INDEX) {
            size = BASE_INDEX;
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
        if (p.page < BASE_INDEX) {
            p.page = BASE_INDEX;
        }
        return p;
    }
}
