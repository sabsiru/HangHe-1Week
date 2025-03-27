package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    private ExecutorService executor;

    // 테스트에 사용할 사용자 ID
    private static final long USER_ID = 1L;

    @BeforeEach
    void setup() {
        // 초기 포인트 1000L로 직접 세팅 (비즈니스 로직 생략)
        userPointTable.insertOrUpdate(USER_ID, 1000L);
    }

    @Test
    void 동시에_포인트_충전시_정상적으로_합산되어야_한다() throws InterruptedException {
        //충전
        long chargeAmount = 100L;

        //쓰레드 수
        int threads = 100;
        executor = Executors.newFixedThreadPool(threads);
        // given: 동시에 시작될 100개의 요청을 기다리기 위한 latch
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    pointService.chargePoint(
                            pointService.getUserPoint(USER_ID),
                            chargeAmount
                    );
                } finally {
                    latch.countDown(); // 작업 완료 알림
                }
            });
        }
        // 모든 요청이 완료될 때까지 대기
        latch.await();

        // then: 최종 포인트는 1000 + (100 * 100) = 11000이어야 함
        UserPoint result = pointService.getUserPoint(USER_ID);
        assertEquals(11000L, result.point());

        executor.shutdown(); // 테스트 후 자원 정리
    }

    @Test
    void 동시_충전_요청이_최대_잔고를_초과하면_초과된_요청은_무시된다() throws InterruptedException {
        // given
        int threads = 95;
        long chargeAmount = 1100L; // (기본 1000 + 1,100 × 90 = 100,000 → 이후 5건은 초과)
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // 예외 수집용 리스트
        Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

        // when
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    pointService.chargePoint(
                            pointService.getUserPoint(USER_ID),
                            chargeAmount
                    );
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 작업 대기

        // then
        UserPoint result = pointService.getUserPoint(USER_ID);
        assertEquals(100_000L, result.point(), "최종 잔고는 최대 잔고(100,000)를 넘지 않아야 한다.");

        // 예외 수: 초과된 5개의 요청은 실패해야 하므로 예외는 최소 5개 이상 발생해야 함
        assertTrue(exceptions.size() >= 5, "최대 잔고 초과로 인해 예외가 발생했어야 함");

        // 예외 메시지도 검증 (모든 예외가 최대 잔고 초과 메시지인지 확인)
        for (Throwable exception : exceptions) {
            assertTrue(exception instanceof PointException);
            assertEquals("충전 가능한 최대 포인트는 100,000입니다.", exception.getMessage());
        }

        executor.shutdown(); // 리소스 정리
    }

    @Test
    void 동시에_포인트_사용시_정상적으로_차감되어야_한다() throws InterruptedException {
        long useAmount = 10L;
        int threads = 100;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    pointService.usePoint(
                            pointService.getUserPoint(USER_ID),
                            useAmount
                    );
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        UserPoint result = pointService.getUserPoint(USER_ID);
        assertEquals(0L, result.point(), "최종 잔고는 0원이 되어야 함");
        assertEquals(0, exceptions.size(), "예외가 발생해서는 안 됨");
    }

    @Test
    void 동시_포인트_사용시_잔고가_부족하면_예외가_발생한다() throws InterruptedException {
        // given
        long useAmount = 20L;
        int threads = 60;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

        // when
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    pointService.usePoint(
                            pointService.getUserPoint(USER_ID),
                            useAmount
                    );
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        UserPoint result = pointService.getUserPoint(USER_ID);
        assertEquals(0L, result.point(), "최종 잔고는 0원이 되어야 함");

        // 예외 검증
        assertEquals(10, exceptions.size(), "잔고 부족으로 10건의 예외가 발생해야 함");

        for (Throwable e : exceptions) {
            assertTrue(e instanceof PointException);
            assertEquals("포인트가 부족합니다.", e.getMessage());
        }
    }
}
