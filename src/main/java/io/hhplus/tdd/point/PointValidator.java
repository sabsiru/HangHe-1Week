package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PointValidator {
    public void validateUserExists(UserPoint userPoint) {
        if (userPoint == null || userPoint.isEmpty()) {
            throw new PointException("USER_NOT_FOUND", "존재하지 않는 사용자입니다.");
        }
    }

    public void validateHistoryExists(List<PointHistory> pointHistories) {
        if (pointHistories.isEmpty()) {
            throw new PointException("POINT_HISTORY_EMPTY", "포인트 충전 및 사용 내역이 없습니다.");
        }
    }


}
