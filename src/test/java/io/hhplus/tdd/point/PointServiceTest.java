package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointService pointService;

    @Test
    public void 사용자의_포인트_조회_성공() throws Exception {
        //given
        UserPoint userPoint = new UserPoint(1L, 5000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(userPoint);

        //when
        UserPoint result = pointService.getUserPoint(userPoint.id());

        //then
        assertEquals(5000L, result.point());
        assertEquals(1L, result.id());

    }

    @Test
    public void 존재하지_않는_사용자는_0_포인트_반환() throws Exception {
        //given
        long id = 99L;
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        //when
        UserPoint result = pointService.getUserPoint(id);

        //then
        assertEquals(0, result.point());
        assertEquals(99L, result.id());

    }

    @Test
    public void 포인트_충전_성공() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 1000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);

        //when
        UserPoint result = pointService.chargePoint(current, 3000L);

        //then
        assertEquals(4000L, result.point());
        assertEquals(1L, result.id());

    }

    @Test
    public void 충전시_최대한도_초과시_예외발생() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 90000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);

        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> pointService.chargePoint(current, 10001L));

        //then
        assertEquals("충전 가능한 최대 포인트는 100,000입니다.", e.getMessage());
    }

    @Test
    public void 충전포인트가_음수일_경우_예외발생() throws Exception{
        //given
        UserPoint current = new UserPoint(1L, 10000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);
        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> pointService.chargePoint(current, -1000L));
        //then
        assertEquals("충전 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 충전포인트가_0일_경우_예외발생() throws Exception{
        //given
        UserPoint current = new UserPoint(1L, 10000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);
        //when
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> pointService.chargePoint(current, 0));
        //then
        assertEquals("충전 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 존재하지_않는_사용자_충전시_예외발생() throws Exception {
        // given
        UserPoint current = UserPoint.empty(1L);

        // when
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.chargePoint(current, 1000L)
        );

        // then
        assertEquals("존재하지 않는 사용자입니다.", e.getMessage());
    }

}