package io.hhplus.tdd.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class PointException extends RuntimeException {
    private final String code;
    public PointException(String code, String msg) {
        super(msg);
        this.code = code;
    }
}
