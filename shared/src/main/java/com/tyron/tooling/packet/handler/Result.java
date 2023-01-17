package com.tyron.tooling.packet.handler;

public class Result<T> {

    public static <T> Result<T> failed(Throwable e) {
        return new Result<>(null, e);
    }

    public static <T> Result<T> success(T t) {
        return new Result<>(t, null);
    }

    private final T result;

    private final Throwable error;

    public Result(T t, Throwable e) {
        this.result = t;
        this.error = e;
    }


    public boolean isSuccessful() {
        return error == null;
    }

    public Throwable getException() {
        return error;
    }

    public T getResult() {
        return result;
    }
}
