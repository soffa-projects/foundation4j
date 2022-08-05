package dev.soffa.foundation.data;

import dev.soffa.foundation.commons.TextUtil;
import lombok.Getter;
import org.checkerframework.com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public final class Criteria {

    private Map<String, Object> binding = ImmutableMap.of();
    private String where = "1=1";

    private Criteria() {
    }

    private Criteria(String where) {
        this.where = where;
    }

    private Criteria(String where, Map<String, Object> binding) {
        this.binding = binding;
        this.where = where;
    }

    public static Criteria of(Map<String, Object> filter) {
        return new Criteria(buildWhere(filter), filter);
    }

    public static Criteria of(String query) {
        return new Criteria(query);
    }

    public static Criteria of(String query, Map<String, Object> binding) {
        return new Criteria(query, binding);
    }


    private static String buildWhere(Map<String, Object> filter) {
        if (filter.isEmpty()) {
            return "1=1";
        }
        List<String> where = new ArrayList<>();
        for (String e : filter.keySet()) {
            where.add(TextUtil.format("%s = :%s", e, e));
        }
        return String.join(" AND ", where);
    }


}
