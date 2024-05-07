package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@Sql(value = {"/schema.sql", "/data.sql"}, executionPhase = BEFORE_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약 정보를 조회한다.")
    @Test
    void findAllTest() {
        // When
        final List<Reservation> reservations = reservationRepository.findAll();

        // Then
        assertThat(reservations).hasSize(16);
    }

    @DisplayName("예약 정보를 저장한다.")
    @Test
    void saveTest() {
        // Given
        final String clientName = "브라운";
        final LocalDate reservationDate = LocalDate.now().plusDays(10);
        final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 10));
        final Theme theme = Theme.of(1L, "테바의 비밀친구", "테바의 은밀한 비밀친구", "대충 테바 사진 링크");
        final Reservation reservation = Reservation.of(clientName, reservationDate, reservationTime, theme);

        // When
        final Reservation savedReservation = reservationRepository.save(reservation);

        // Then
        final List<Reservation> reservations = reservationRepository.findAll();
        assertAll(
                () -> assertThat(reservations).hasSize(17),
                () -> assertThat(savedReservation.getId()).isEqualTo(17L),
                () -> assertThat(savedReservation.getClientName()).isEqualTo(reservation.getClientName()),
                () -> assertThat(savedReservation.getDate()).isEqualTo(reservation.getDate()),
                () -> assertThat(savedReservation.getTime()).isEqualTo(reservation.getTime())
        );
    }

    @DisplayName("예약 정보를 삭제한다.")
    @Test
    void deleteByIdTest() {
        // When
        final int deletedDataCount = reservationRepository.deleteById(1L);

        // Then
        assertThat(deletedDataCount).isEqualTo(1);
    }

    @DisplayName("특정 날짜와 시간 아이디를 가진 예약이 존재하는지 조회한다.")
    @Test
    void existByDateAndTimeIdTest() {
        // Given
        final LocalDate reservationDate = LocalDate.now().plusDays(2);
        final Long timeId = 4L;
        final Long themeId = 9L;

        // When
        final boolean isExist = reservationRepository.existByDateAndTimeIdAndThemeId(reservationDate, timeId, themeId);

        // Then
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 시간 아이디를 가진 예약이 존재하는지 조회한다.")
    @Test
    void existByTimeIdTest() {
        // Given
        final Long timeId = 1L;

        // When
        final boolean isExist = reservationRepository.existByTimeId(timeId);

        // Then
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 날짜와 테마 아이디를 가진 예약 정보를 모두 조회한다.")
    @Test
    void findAllByDateAndThemeIdTest() {
        // Given
        final LocalDate reservationDate = LocalDate.now().minusDays(3);
        final Long themeId = 1L;

        // When
        final List<Reservation> allByDateAndThemeId = reservationRepository.findAllByDateAndThemeId(reservationDate, themeId);

        // Then
        assertThat(allByDateAndThemeId).hasSize(2);
    }
}
