package dev.soffa.foundation.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static dev.soffa.foundation.model.PagingConstants.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {


    @Schema(defaultValue =  BASE_INDEX_S, nullable = true)
    @Parameter(in = ParameterIn.QUERY)
    private int page;

    @Schema(defaultValue = DEFAULT_FETCH_SIZE_S, nullable = true)
    private int size;

    @Schema(nullable = true)
    private String sort;

    public Paging(int page, int size) {
        this.page = page;
        this.size = Math.min(size, DEFAULT_MAX_SIZE);
    }

    public static Paging of(Paging paging) {
        return of(paging, "id");
    }

    public static Paging of(Paging paging, String defautlSort) {
        Paging p = paging;
        if (p == null) {
            p = new Paging(BASE_INDEX, DEFAULT_FETCH_SIZE);
        }
        if (p.sort == null) {
            p.sort = defautlSort;
        }
        return p.cap();
    }

    public Paging cap() {
        if (size > DEFAULT_MAX_SIZE) {
            size = DEFAULT_MAX_SIZE;
        } else if (size < BASE_INDEX) {
            size = DEFAULT_FETCH_SIZE;
        }
        if (page < BASE_INDEX) {
            page = BASE_INDEX;
        }
        return this;
    }
}
