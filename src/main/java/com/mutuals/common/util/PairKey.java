package com.mutuals.common.util;

public record PairKey(Long lowId, Long highId) {

    public static PairKey of(Long first, Long second) {
        return first < second ? new PairKey(first, second) : new PairKey(second, first);
    }
}
