package dev.soffa.foundation.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ItemList<T> {

    private List<T> data;

}
