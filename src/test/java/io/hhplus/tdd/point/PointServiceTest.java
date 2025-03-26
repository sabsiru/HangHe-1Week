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

}