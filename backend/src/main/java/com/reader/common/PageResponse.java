package com.reader.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> items, long total, int page, int size) {

    public static <E, T> PageResponse<T> of(Page<E> source, Function<E, T> mapper) {
        return new PageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getTotalElements(),
                source.getNumber(),
                source.getSize());
    }
}
