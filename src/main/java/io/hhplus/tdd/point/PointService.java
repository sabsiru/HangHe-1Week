package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PointValidator validator;

    // 사용자별 락 저장소
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    //포인트 조회
    public UserPoint getUserPoint(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);

        //사용자 검증
        validator.validateUserExists(userPoint);

        return userPointTable.selectById(userId);
    }

    //포인트 충전
    public UserPoint chargePoint(UserPoint current, long amount) {
        validator.validateUserExists(current);

        long userId = current.id();
        // 락 획득 또는 생성
        ReentrantLock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock());
        lock.lock(); // 락 시작
        try {
            UserPoint userPoint = userPointTable.selectById(current.id());
            UserPoint charged = UserPoint.charge(userPoint, amount);

            userPointTable.insertOrUpdate(charged.id(), charged.point());
            pointHistoryTable.insert(charged.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());
            return charged;
        } finally {
            lock.unlock(); // 락 해제
        }
    }

    //포인트 사용
    public UserPoint usePoint(UserPoint current, long amount) {
        validator.validateUserExists(current);

        UserPoint userPoint = userPointTable.selectById(current.id());
        UserPoint used = UserPoint.use(userPoint, amount);

        userPointTable.insertOrUpdate(used.id(), used.point());
        pointHistoryTable.insert(used.id(), amount, TransactionType.USE, System.currentTimeMillis());

        return used;
    }

    //포인트 내역 조회
    public List<PointHistory> getPointHistories(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        //사용자 검증
        validator.validateUserExists(userPoint);

        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);

        //히스토리 검증
        validator.validateHistoryExists(pointHistories);

        return pointHistories;
    }
}
