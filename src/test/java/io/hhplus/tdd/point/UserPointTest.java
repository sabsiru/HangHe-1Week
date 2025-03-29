package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserPointTest {

    @Test
    public void 충전_성공() throws Exception {
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        //when
        UserPoint after = userPoint.charge(userPoint, 2000L);

        //then
        assertEquals(after.point(), 3000L);

    }

    @Test
    public void 충전시_최대한도_초과시_예외발생() throws Exception {
        //given
        UserPoint userPoint = new UserPoint(1L, 90000L, System.currentTimeMillis());

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserPoint.charge(userPoint, 10001L)
        );

        //then
        assertEquals("충전 가능한 최대 포인트는 100,000입니다.", e.getMessage());

    }

    @Test
    public void 충전포인트가_음수일시_예외발생() {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userPoint.charge(userPoint, -1000L)
        );

        //then
        assertEquals("충전 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 충전포인트가_0일시_예외발생() {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userPoint.charge(userPoint, 0)
        );

        //then
        assertEquals("충전 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 사용_성공() throws Exception {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        //when
        UserPoint after = userPoint.use(userPoint, 2000L);

        //then
        assertEquals(after.point(), 3000L);

    }

    @Test
    public void 사용시_남은_포인트가_0미만이면_예외발생() throws Exception {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> UserPoint.use(userPoint, 5001L)
        );

        //then
        assertEquals("포인트가 부족합니다.", e.getMessage());

    }

    @Test
    public void 사용_포인트가_음수일시_예외발생() {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userPoint.use(userPoint, -1000L)
        );

        //then
        assertEquals("사용 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 사용_포인트가_0일시_예외발생() {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userPoint.use(userPoint, 0)
        );

        //then
        assertEquals("사용 금액은 1 이상이어야 합니다.", e.getMessage());
    }
}