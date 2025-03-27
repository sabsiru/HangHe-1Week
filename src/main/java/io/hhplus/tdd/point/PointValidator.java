package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

@Component
public class PointValidator {
    public void validateUserExists(UserPoint userPoint) {
        if (userPoint == null || userPoint.isEmpty()) {
            throw new PointException("USER_NOT_FOUND", "존재하지 않는 사용자입니다.");
        }
    }

}
