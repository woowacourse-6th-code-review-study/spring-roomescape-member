package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.controller.request.ThemeRequest;
import roomescape.controller.response.ThemeResponse;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ThemeService {
    public static final int START_DATE_DIFF = 8;
    public static final int END_DATE_DIFF = 1;
    public static final int TOP_LIMIT_COUNT = 10;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse save(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toEntity();

        Theme savedTheme = themeRepository.save(theme);

        return ThemeResponse.from(savedTheme);
    }

    public void deleteById(long id) {
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findTops() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(START_DATE_DIFF);
        LocalDate endDate = today.minusDays(END_DATE_DIFF);
        List<Reservation> reservations = reservationRepository.findByPeriod(startDate, endDate);

        return findTopReservations(reservations, TOP_LIMIT_COUNT).stream()
                .map(ThemeResponse::from)
                .toList();
    }

    private List<Theme> findTopReservations(List<Reservation> reservations, int limitCount) {
        return reservations.stream()
                .collect(groupingBy(Reservation::getTheme, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limitCount)
                .map(Map.Entry::getKey)
                .toList();
    }
}
