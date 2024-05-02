package roomescape.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResult> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(new ErrorResult(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> handleException(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(new ErrorResult(ex.getMostSpecificCause().getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> handleException(Exception ex) {
        return new ResponseEntity<>(new ErrorResult("죄송합니다. 서버에서 문제가 발생하여 요청을 처리할 수 없습니다."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
