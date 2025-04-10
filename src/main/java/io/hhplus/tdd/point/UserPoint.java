package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, 0);
    }

    public static UserPoint charge(UserPoint current, long amount) {
        if (amount <= 0) {
            throw new PointException("INVALID_AMOUNT", "충전 금액은 1 이상이어야 합니다.");
        }

        long newPoint = current.point() + amount;
        if (newPoint > 100_000L) {
            throw new PointException("MAX_POINT_LIMIT", "충전 가능한 최대 포인트는 100,000입니다.");
        }

        return new UserPoint(current.id(), newPoint, System.currentTimeMillis());
    }

    public static UserPoint use(UserPoint current, long amount) {

        long newPoint = current.point() - amount;
        if (newPoint < 0) {
            throw new PointException("NOT_ENOUGH_AMOUNT", "포인트가 부족합니다.");
        }

        if (amount <= 0) {
            throw new PointException("INVALID_AMOUNT", "사용 금액은 1 이상이어야 합니다.");
        }

        return new UserPoint(current.id(), newPoint, System.currentTimeMillis());
    }

    public boolean isEmpty() {
        return this.point == 0&& this.updateMillis == 0;
    }
}
