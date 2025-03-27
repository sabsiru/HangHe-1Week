package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    UserPointTable userPointTable;

    @Autowired
    PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable.insertOrUpdate(1L, 5000L);
        userPointTable.insertOrUpdate(2L, 5000L);
    }

    @Test
    public void 포인트_정상조회() throws Exception {
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(5000))
                .andExpect(jsonPath("$.updateMillis").isNumber());

    }

    @Test
    public void 사용자_ID의_TYPE이_LONG이_아닐때_예외() throws Exception{
        mockMvc.perform(get("/point/A")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ID"))
                .andExpect(jsonPath("$.message").value("ID는 숫자여야 합니다."));
    }

    @Test
    public void 존재하지_않는_사용자_조회_요청시_예외발생() throws Exception{
        mockMvc.perform(get("/point/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @Test
    public void 포인트_충전_성공() throws Exception {
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10000")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(15000));
    }

    @Test
    public void 포인트_충전_누적_정상작동() throws Exception {
        // 첫 번째 충전: 10,000 + 기본값 5,000
        mockMvc.perform(
                        patch("/point/1/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("10000")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(15000));

        // 두 번째 충전: 35,000 → 누적 45,000
        mockMvc.perform(
                        patch("/point/1/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("30000")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(45000));
    }

    @Test
    public void 포인트_충전_최대한도_초과_예외발생() throws Exception {
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("95000"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MAX_POINT_LIMIT"))
                .andExpect(jsonPath("$.message").value("충전 가능한 최대 포인트는 100,000입니다."));

    }

    @Test
    public void 충전할_포인트가_0일때_예외발생() throws Exception {
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.message").value("충전 금액은 1 이상이어야 합니다."));
    }

    @Test
    public void 충전할_포인트가_음수일때_예외발생() throws Exception {
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("-1000")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.message").value("충전 금액은 1 이상이어야 합니다."));
    }

    @Test
    public void 존재하지_않는_사용자_충전_요청시_예외발생() throws Exception {
        mockMvc.perform(
                        patch("/point/-1/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("1000")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    @Test
    public void 포인트_정상_사용() throws Exception {
        mockMvc.perform(
                        patch("/point/1/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(4000));

    }

    @Test
    public void 포인트_누적_사용_정상_작동() throws Exception {
        // 첫 번째 사용 5000 -> 4000
        mockMvc.perform(
                        patch("/point/1/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(4000));

        // 두 번째 사용 4000 - > 3000
        mockMvc.perform(
                        patch("/point/1/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(3000));

    }

    @Test
    public void 포인트_사용시_잔고가_0미만일시_예외발생() throws Exception {
        mockMvc.perform(
                        patch("/point/1/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("5001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOT_ENOUGH_AMOUNT"))
                .andExpect(jsonPath("$.message").value("포인트가 부족합니다."));


    }

    @Test
    public void 사용_포인트가_음수일_경우_예외발생() throws Exception{
        mockMvc.perform(
                        patch("/point/1/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("-100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.message").value("사용 금액은 1 이상이어야 합니다."));

    }

    @Test
    public void 사용_포인트가_0일_경우_예외발생() throws Exception{
        mockMvc.perform(
                        patch("/point/1/use")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.message").value("사용 금액은 1 이상이어야 합니다."));

    }

    @Test
    public void 존재하지_않는_사용자_사용_요청시_예외발생() throws Exception{
        mockMvc.perform(patch("/point/999/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

}
