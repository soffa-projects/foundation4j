package dev.soffa.foundation.model;

public interface PagingConstants {

    int BASE_INDEX = 1;
    String BASE_INDEX_S = "1";
    int DEFAULT_MAX_SIZE = 10_000;
    int DEFAULT_FETCH_SIZE = 50;
    String DEFAULT_FETCH_SIZE_S = "50";

    Paging DEFAULT = new Paging(BASE_INDEX, DEFAULT_FETCH_SIZE);

}
