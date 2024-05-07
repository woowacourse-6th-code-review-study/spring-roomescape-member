package roomescape.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

import static org.springframework.web.client.HttpServerErrorException.InternalServerError;

@RestControllerAdvice
public class BuiltInExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleNoSuchElementException(final NoSuchElementException e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleInternalServerError(final InternalServerError e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("예기치 못한 서버 에러가 발생하였습니다."));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("예기치 못한 에러가 발생하였습니다."));
    }
}
