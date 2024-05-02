package roomescape.domain;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationDate {

    private final LocalDate date;

    public ReservationDate(LocalDate date) {
        validate(date);
        this.date = date;
    }

    private void validate(LocalDate date) {
        validateNonNull(date);
        validateNonPastDate(date);
    }

    private void validateNonNull(LocalDate date) {
        if (date == null) {
            throw new NullPointerException("날짜는 null일 수 없습니다");
        }
    }

    private void validateNonPastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(date + ": 예약 날짜는 현재 보다 이전일 수 없습니다");
        }
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationDate that = (ReservationDate) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
