package com.demo3.utils;

import lombok.Data;

@Data
public class Res<T> {
    private Integer code;
    private T data;
    private String message;

    public Res() {
    }

    public Res(T data) {
        this.code = 0;
        this.data = data;
        this.message = "ok";
    }

    public Res(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Res<T> createSuccess(T data) {
        return new Res<>(data);
    }

    public static <T> Res<T> createError(String message) {
        return new Res<>(500, null, message);
    }
}
