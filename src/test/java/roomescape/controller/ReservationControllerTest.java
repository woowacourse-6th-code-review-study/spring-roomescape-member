package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.ReservationResponse;
import roomescape.dto.SaveReservationRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(value = {"/schema.sql", "/data.sql"}, executionPhase = BEFORE_TEST_METHOD)
class ReservationControllerTest {

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    public void initReservation() {
        RestAssured.port = randomServerPort;
    }

    @DisplayName("전체 예약 정보를 조회한다.")
    @Test
    void getReservationsTest() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(16));
    }

    @DisplayName("예약 정보를 저장한다.")
    @Test
    void saveReservationTest() {
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now().plusDays(1),
                "브라운",
                1L,
                1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(saveReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(17));
    }

    @DisplayName("예약 정보를 삭제한다.")
    @Test
    void deleteReservationTest() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        final List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        assertThat(reservations.size()).isEqualTo(15);
    }

    @DisplayName("존재하지 않는 예약 정보를 삭제하려고 하면 400코드가 응답된다.")
    @Test
    void deleteNoExistReservationTest() {
        RestAssured.given().log().all()
                .when().delete("/reservations/20")
                .then().log().all()
                .statusCode(400)
                .body("message", is("해당 id의 예약이 존재하지 않습니다."));
    }

    @DisplayName("존재하지 않는 예약 시간을 포함한 예약 저장 요청을 하면 400코드가 응답된다.")
    @Test
    void saveReservationWithNoExistReservationTime() {
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now().plusDays(1),
                "브라운",
                80L,
                1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(saveReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", is("해당 id의 예약 시간이 존재하지 않습니다."));
    }

    @DisplayName("현재 날짜보다 이전 날짜의 예약을 저장하려고 요청하면 400코드가 응답된다.")
    @Test
    void saveReservationWithReservationDateAndTimeBeforeNow() {
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now().minusDays(1),
                "브라운",
                1L,
                1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(saveReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", is("현재 날짜보다 이전 날짜를 예약할 수 없습니다."));
    }

    @DisplayName("유효하지 않은 사용자 이름을 포함한 예약 저장 요청을 하면 400코드가 응답된다.")
    @Test
    void saveReservationWithInvalidName() {
        final SaveReservationRequest saveReservationRequest = new SaveReservationRequest(
                LocalDate.now().plusDays(1),
                "브라운운우눈우우웅우ㅜㅇ",
                1L,
                1L
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(saveReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", is("예약자 이름은 1글자 이상 5글자 이하여야 합니다."));
    }
}
