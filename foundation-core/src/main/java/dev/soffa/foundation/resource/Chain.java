package dev.soffa.foundation.resource;

public interface Chain<I, O> {

    O next(I input);

}
