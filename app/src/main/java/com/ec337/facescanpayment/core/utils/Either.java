package com.ec337.facescanpayment.core.utils;

import java.util.function.Function;

// ref: https://github.com/vavr-io/vavr/blob/master/src/main/java/io/vavr/control/Either.java
public class Either<L, R> {
    private final L left;
    private final R right;

    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(value, null);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(null, value);
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public <T> Either<L, T> map(Function<? super R, ? extends T> mapper) {
        if (isRight()) {
            return right(mapper.apply(right));
        } else {
            return left(left);
        }
    }

    public <T> Either<L, T> flatMap(Function<? super R, Either<L, T>> mapper) {
        if (isRight()) {
            return mapper.apply(right);
        } else {
            return left(left);
        }
    }

    public <T> T fold(Function<? super L, T> leftMapper, Function<? super R, T> rightMapper) {
        if (isLeft()) {
            return leftMapper.apply(left);
        } else {
            return rightMapper.apply(right);
        }
    }
}