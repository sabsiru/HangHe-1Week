package io.hhplus.tdd.point;

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

    @BeforeEach
    void setUp() {
        userPointTable.insertOrUpdate(1L, 5000L);
    }

    @Test
    public void 포인트_정상조회() throws Exception{
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(5000))
                .andExpect(jsonPath("$.updateMillis").isNumber());

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
    public void 포인트_충전_누적_정상작동() throws Exception{
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
        //given
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("95000"))
                .andExpect(status().isOk());

        //when
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MAX_POINT_LIMIT"))
                .andExpect(jsonPath("$.message").value("충전 가능한 최대 포인트는 100,000입니다."));

    }

    @Test
    public void 충전할_포인트가_0일때_예외발생() throws Exception{
        //given
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.message").value("충전 금액은 1 이상이어야 합니다."));
    }

    @Test
    public void 충전할_포인트가_음수일때_예외발생() throws Exception{
        //given
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("-1000")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
                .andExpect(jsonPath("$.message").value("충전 금액은 1 이상이어야 합니다."));
    }

    @Test
    public void 존재하지_않는_사용자_충전_요청시_예외발생() throws Exception{
        mockMvc.perform(
                        patch("/point/-1/charge")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("1000")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }
}
