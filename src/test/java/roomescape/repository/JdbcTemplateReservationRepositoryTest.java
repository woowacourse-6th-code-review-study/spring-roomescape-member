package roomescape.repository;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@SpringBootTest
class JdbcTemplateReservationRepositoryTest {
    private static final ReservationTime DEFAULT_TIME = new ReservationTime(1L, LocalTime.of(11, 56));
    private static final Theme DEFAULT_THEME = new Theme(1L, "이름", "설명", "http://썸네일");

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES('11:56')");

        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail) VALUES('name', 'description', 'http://thumbnail')");

    }

    @Test
    @DisplayName("Reservation 을 잘 저장하는지 확인한다.")
    void save() {
        var beforeSave = reservationRepository.findAll();
        Reservation saved = reservationRepository.save(
                new Reservation("test", LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME));
        var afterSave = reservationRepository.findAll();

        Assertions.assertThat(afterSave)
                .containsAll(beforeSave)
                .contains(saved);
    }

    @Test
    @DisplayName("Reservation 을 잘 조회하는지 확인한다.")
    void findAll() {
        List<Reservation> beforeSave = reservationRepository.findAll();
        reservationRepository.save(new Reservation("test", LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME));
        reservationRepository.save(new Reservation("test2", LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME));

        List<Reservation> afterSave = reservationRepository.findAll();
        Assertions.assertThat(afterSave.size())
                .isEqualTo(beforeSave.size() + 2);
    }

    @Test
    @DisplayName("Reservation 을 잘 지우는지 확인한다.")
    void delete() {
        List<Reservation> beforeSaveAndDelete = reservationRepository.findAll();
        reservationRepository.save(new Reservation("test", LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME));

        reservationRepository.delete(1L);

        List<Reservation> afterSaveAndDelete = reservationRepository.findAll();

        Assertions.assertThat(beforeSaveAndDelete)
                .containsExactlyElementsOf(afterSaveAndDelete);
    }

    @Test
    @DisplayName("특정 테마에 특정 날짜 특정 시간에 예약 여부를 잘 반환하는지 확인한다.")
    void existsByThemeAndDateAndTime() {
        LocalDate date1 = LocalDate.now();
        LocalDate date2 = date1.plusDays(1);
        reservationRepository.save(new Reservation("name", date1, DEFAULT_TIME, DEFAULT_THEME));

        assertAll(
                () -> Assertions.assertThat(
                                reservationRepository.existsByThemeAndDateAndTime(DEFAULT_THEME, date1, DEFAULT_TIME))
                        .isTrue(),
                () -> Assertions.assertThat(
                                reservationRepository.existsByThemeAndDateAndTime(DEFAULT_THEME, date2, DEFAULT_TIME))
                        .isFalse()
        );
    }

    @Test
    @DisplayName("특정 시간에 예약이 있는지 확인한다.")
    void existsByTime() {
        LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation("name", date, DEFAULT_TIME, DEFAULT_THEME));

        assertAll(
                () -> Assertions.assertThat(reservationRepository.existsByTime(DEFAULT_TIME))
                        .isTrue(),
                () -> Assertions.assertThat(
                                reservationRepository.existsByTime(new ReservationTime(2L, LocalTime.of(12, 56))))
                        .isFalse()
        );
    }

    @Test
    @DisplayName("특정 테마에 예약이 있는지 확인한다.")
    void existsByTheme() {
        LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation("name", date, DEFAULT_TIME, DEFAULT_THEME));

        assertAll(
                () -> Assertions.assertThat(reservationRepository.existsByTheme(DEFAULT_THEME))
                        .isTrue(),
                () -> Assertions.assertThat(reservationRepository.existsByTheme(new Theme(2L, DEFAULT_THEME)))
                        .isFalse()
        );
    }
}
