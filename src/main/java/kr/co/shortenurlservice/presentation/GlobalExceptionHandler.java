package kr.co.shortenurlservice.presentation;

import kr.co.shortenurlservice.domain.LackOfShortenUrlKeyException;
import kr.co.shortenurlservice.domain.NotFoundShortenUrlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.View;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LackOfShortenUrlKeyException.class)
    public ResponseEntity<String> handleLackOfShortenUrlKeyException(
            LackOfShortenUrlKeyException ex
    ) {
        //충분히 치명적일 수 있는 상황이라, 로그를 남긴다고 하면 에러 레벨의 로그를 남기는게 적절합니다.
        //만약 강사님이라면, 개발자에게 바로 알림을 줄 수 있는 수단을 호출할 것 같다고 한다.
        log.error("단축 URL 자원이 부족합니다.");
        return new ResponseEntity<>("단축 URL 자원이 부족합니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundShortenUrlException.class)
    public ResponseEntity<String> handleNotFoundShortenUrlException(
            NotFoundShortenUrlException ex
    ) {
        //만약 초당 N개 이상이 발생한다면, 시스템에 문제가 있다고 판단하여 warn 레벨을 사용할 수도 있다.
        //사용자가 아무렇게나 요청하면 발생하는 경우이므로, 에러 레벨은는 적절하지 않다.
        //그런데 만약, 여기서 발생한 에러가 절대 실수할 수 없는 시스템과 시스템 사이의 호출이라면 그때는 에러 레벨이 적절하다.
        //예외가 발생했다고 그걸 무조건 에러 레벨로 찍는 것은 아니다!
        //인포와 에러 레벨의 차이는 개발자가 직접 개입해야하는 상황인지를 생각해보면 된다.
        //참고로 fatal 수준의 에러는 직접 남길 수 없다. 시스템 자체적으로 던지게 되어있다.
        log.info(ex.getMessage());
        return new ResponseEntity<>("단축 URL을 찾지 못했습니다.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        StringBuilder errorMessage = new StringBuilder("유효성 검증 실패: ");
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessage.append(String.format("필드 '%s': %s. ", error.getField(), error.getDefaultMessage()));
        });

        log.debug("잘못된 요청: {}", errorMessage);

        //클라이언트에 응답
        return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
    }

}
