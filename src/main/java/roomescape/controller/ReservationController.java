package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationCreateRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readReservations() {
        List<ReservationResponse> response = service.readReservations().stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationCreateRequest dto) {
        Reservation reservation = service.createReservation(dto);
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/reservations/" + reservation.getId())
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long id) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
