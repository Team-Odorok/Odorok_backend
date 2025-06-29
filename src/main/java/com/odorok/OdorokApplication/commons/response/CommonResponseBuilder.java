package com.odorok.OdorokApplication.commons.response;

public final class CommonResponseBuilder {
    //성공 응답
    public static <T> ResponseRoot<T> success(String message, T data) {
        return success("success", message, data);
    }

    //성공 응답(no data)
    public static <T> ResponseRoot<T> success(String message) {
        return ResponseRoot.<T>builder().status("success").message(message).build();
    }

    // 성공 응답 (with data, success 외 다른 상태)
    public static <T> ResponseRoot<T> success(String status, String message, T data) {
        return ResponseRoot.<T>builder().status(status).message(message).data(data).build();
    }

    // 실패 응답 생성
    public static <T> ResponseRoot<T> fail(String message) {
        return ResponseRoot.<T>builder().status("fail").message(message).build();
    }

    private CommonResponseBuilder() {}
}
