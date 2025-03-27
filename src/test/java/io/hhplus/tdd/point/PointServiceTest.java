package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private PointValidator validator;

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
        PointException e = assertThrows(PointException.class,
                () -> pointService.chargePoint(current, 10001L));

        //then
        assertEquals("충전 가능한 최대 포인트는 100,000입니다.", e.getMessage());
    }

    @Test
    public void 충전포인트가_음수일_경우_예외발생() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 10000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);
        //when
        PointException e = assertThrows(PointException.class,
                () -> pointService.chargePoint(current, -1000L));
        //then
        assertEquals("충전 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 충전포인트가_0일_경우_예외발생() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 10000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);
        //when
        PointException e = assertThrows(PointException.class,
                () -> pointService.chargePoint(current, 0));
        //then
        assertEquals("충전 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 존재하지_않는_사용자_충전시_예외발생() throws Exception {
        // given
        long userId = 1L;
        UserPoint current = UserPoint.empty(userId);

        doThrow(new PointException("USER_NOT_FOUND", "존재하지 않는 사용자입니다."))
                .when(validator).validateUserExists(current);

        // when
        PointException e = assertThrows(PointException.class, () ->
                pointService.chargePoint(current, 1000L)
        );

        // then
        assertEquals("존재하지 않는 사용자입니다.", e.getMessage());
    }

    @Test
    public void 포인트_사용_성공() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 5000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);

        //when
        UserPoint used = pointService.usePoint(current, 3000L);

        //then
        assertEquals(2000L, used.point());
        assertEquals(1L, used.id());
    }

    @Test
    public void 포인트_사용시_잔고가_0미만일시_예외발생() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 1000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);

        //when
        PointException e = assertThrows(PointException.class,
                () -> pointService.usePoint(current, 1001L));

        //then
        assertEquals("포인트가 부족합니다.", e.getMessage());
    }

    @Test
    public void 사용포인트가_음수일_경우_예외발생() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 10000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);
        //when
        PointException e = assertThrows(PointException.class,
                () -> pointService.usePoint(current, -1000L));
        //then
        assertEquals("사용 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 사용포인트가_0일_경우_예외발생() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 10000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);
        //when
        PointException e = assertThrows(PointException.class,
                () -> pointService.usePoint(current, 0));
        //then
        assertEquals("사용 금액은 1 이상이어야 합니다.", e.getMessage());
    }

    @Test
    public void 존재하지_않는_사용자_사용시_예외발생() throws Exception {
        // given
        long userId = 1L;
        UserPoint current = UserPoint.empty(userId);

        doThrow(new PointException("USER_NOT_FOUND", "존재하지 않는 사용자입니다."))
                .when(validator).validateUserExists(current);

        // when
        PointException e = assertThrows(PointException.class, () ->
                pointService.usePoint(current, 1000L)
        );

        // then
        assertEquals("존재하지 않는 사용자입니다.", e.getMessage());
    }

    @Test
    public void 충전_성공시_히스토리_저장_성공() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 1000L, System.currentTimeMillis());

        when(userPointTable.selectById(1L)).thenReturn(current);

        //when
        UserPoint charged = pointService.chargePoint(current, 3000L);

        //then
        verify(pointHistoryTable, times(1)).insert(
                eq(1L),
                eq(3000L),
                eq(TransactionType.CHARGE),
                anyLong()
        );
    }

    @Test
    public void 포인트_사용성공시_히스토리_저장_성공() throws Exception {
        //given
        UserPoint current = new UserPoint(1L, 4000L, System.currentTimeMillis());
        when(userPointTable.selectById(1L)).thenReturn(current);

        //when
        UserPoint used = pointService.usePoint(current, 4000L);

        //then
        verify(pointHistoryTable, times(1)).insert(
                eq(1L),
                eq(4000L),
                eq(TransactionType.USE),
                anyLong()
        );
    }

    @Test
    public void 포인트_충전및사용_내역_조회_성공() throws Exception {
        //given
        long userId = 1L;
        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, userId, 3000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 2000L, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistories);

        //when
        List<PointHistory> result = pointService.getPointHistories(userId);

        //then
        assertEquals(2, result.size());
        assertEquals(3000L, result.get(0).amount());
        assertEquals(TransactionType.CHARGE, result.get(0).type());

        assertEquals(2000L, result.get(1).amount());
        assertEquals(TransactionType.USE, result.get(1).type());

    }

    @Test
    public void 포인트_충전및사용_히스토리가_없을_경우_예외발생() throws Exception {
        // given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        when(userPointTable.selectById(eq(userId))).thenReturn(userPoint);
        when(pointHistoryTable.selectAllByUserId(eq(userId))).thenReturn(Collections.emptyList());

        doThrow(new PointException("POINT_HISTORY_EMPTY", "포인트 충전 및 사용 내역이 없습니다."))
                .when(validator).validateHistoryExists(Collections.emptyList());

        // when
        PointException e = assertThrows(PointException.class, () -> {
            pointService.getPointHistories(userId);
        });

        // then
        assertEquals("포인트 충전 및 사용 내역이 없습니다.", e.getMessage());
    }
}