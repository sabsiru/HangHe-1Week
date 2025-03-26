package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    //포인트 조회
    public UserPoint getUserPoint(long userId) {
        return userPointTable.selectById(userId);
    }

    //포인트 충전
    public UserPoint chargePoint(UserPoint current, long amount) {
        if (current.isEmpty()) {
            throw new PointException("USER_NOT_FOUND","존재하지 않는 사용자입니다.");
        }

        UserPoint userPoint = userPointTable.selectById(current.id());
        UserPoint charged = UserPoint.charge(userPoint, amount);

        userPointTable.insertOrUpdate(charged.id(), charged.point());
        pointHistoryTable.insert(charged.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());
        return charged;
    }

    //포인트 사용
    public UserPoint usePoint(UserPoint current, long amount) {
        if (current.isEmpty()) {
            throw new PointException("USER_NOT_FOUND","존재하지 않는 사용자입니다.");
        }

        UserPoint userPoint = userPointTable.selectById(current.id());
        UserPoint used = UserPoint.use(userPoint, amount);

        userPointTable.insertOrUpdate(used.id(), used.point());
        pointHistoryTable.insert(used.id(), amount, TransactionType.USE, System.currentTimeMillis());

        return used;
    }

    //포인트 내역 조회
    public List<PointHistory> getPointHistories(long userId) {
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);

        if (pointHistories.isEmpty()) {
            throw new PointException("POINT_HISTORY_EMPTY","포인트 충전 및 사용 내역이 없습니다.");
        }
        return pointHistories;
    }
}
