package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.PAST_TIME;
import static roomescape.exception.ExceptionType.RESERVATION_TIME_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ThemeResponse;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    //ToDo 테스트 작성
    public ReservationResponse save(ReservationRequest reservationRequest) {
        //TODO 변수명
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(ExceptionType.THEME_NOT_FOUND));

        Reservation beforeSave = new Reservation(
                reservationRequest.name(),
                reservationRequest.date(),
                reservationTime,
                theme
        );
        boolean isDuplicate = reservationRepository.findAll()
                .stream()
                .anyMatch(reservation -> validateDuplicateReservation(beforeSave, reservation));
        if (isDuplicate) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }

        //todo 테스트
        if (isBefore(beforeSave)) {
            throw new RoomescapeException(PAST_TIME);
        }

        Reservation saved = reservationRepository.save(beforeSave);
        return toResponse(saved);
    }

    private boolean validateDuplicateReservation(Reservation beforeSave, Reservation reservation) {
        return reservation.isSameDateTime(beforeSave)
                && beforeSave.isSameTheme(reservation);
    }

    //TODO : 도메인에게 넘길 수 있을 것 같은데
    private static boolean isBefore(Reservation beforeSave) {
        return LocalDateTime.of(beforeSave.getDate(), beforeSave.getTime())
                .isBefore(LocalDateTime.now());
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationTime reservationTime = reservation.getReservationTime();
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(reservationTime.getId(),
                reservation.getTime());
        Theme theme = reservation.getTheme();
        ThemeResponse themeResponse = new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(),
                theme.getThumbnail());
        return new ReservationResponse(reservation.getId(),
                reservation.getName(), reservation.getDate(), reservationTimeResponse, themeResponse);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(long id) {
        reservationRepository.delete(id);
    }
}
