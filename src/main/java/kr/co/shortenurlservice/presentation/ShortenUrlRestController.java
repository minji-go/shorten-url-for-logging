package kr.co.shortenurlservice.presentation;

import kr.co.shortenurlservice.application.SimpleShortenUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RestController
public class ShortenUrlRestController {

    private SimpleShortenUrlService simpleShortenUrlService;

    @Autowired
    ShortenUrlRestController(SimpleShortenUrlService simpleShortenUrlService) {
        this.simpleShortenUrlService = simpleShortenUrlService;
    }

    /** 단축URL 생성 */
    @RequestMapping(value = "/shortenUrl", method = RequestMethod.POST)
    public ResponseEntity<ShortenUrlCreateResponseDto> createShortenUrl(
            @Valid @RequestBody ShortenUrlCreateRequestDto shortenUrlCreateRequestDto
    ) {
        //1. System.out.println > 실무에서 권장되지 않는 방식. 부록 참조
        //System.out.println(shortenUrlCreateRequestDto.getORriginalUrl());
        //2. Slf4j
        //log.info("createShortenUrl");
        //3. trace 레벨로 찍고, Dto를 통째로 찍을 것 같다.
        //   이렇게 하면 어떤 요청이 들어왔는지 알수도있고, 그대로 요청해서 로컬에서 디버깅 할 수도 있다.
        //   하지만 로그 사이즈가 엄청 커질 수 있다. 이는 레벨 단위로 보관 기간을 전략적으로 선택하면 된다.
        log.trace("shortenUrlCreateRequestDto {}", shortenUrlCreateRequestDto);
        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto =
                simpleShortenUrlService.generateShortenUrl(shortenUrlCreateRequestDto);
        return ResponseEntity.ok(shortenUrlCreateResponseDto);
    }

    /** 단축URL 접속시 리다이렉트 */
    @RequestMapping(value = "/{shortenUrlKey}", method = RequestMethod.GET)
    public ResponseEntity<?> redirectShortenUrl(
            @PathVariable String shortenUrlKey
    ) throws URISyntaxException {
        String originalUrl = simpleShortenUrlService.getOriginalUrlByShortenUrlKey(shortenUrlKey);

        URI redirectUri = new URI(originalUrl);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(redirectUri);

        return new ResponseEntity<>(httpHeaders, HttpStatus.MOVED_PERMANENTLY);
    }

    /** 단축URL 리다이렉트 횟수 및 원본 URL 조회 */
    @RequestMapping(value = "/shortenUrl/{shortenUrlKey}", method = RequestMethod.GET)
    public ResponseEntity<ShortenUrlInformationDto> getShortenUrlInformation(
            @PathVariable String shortenUrlKey
    ) {
        ShortenUrlInformationDto shortenUrlInformationDto =
                simpleShortenUrlService.getShortenUrlInformationByShortenUrlKey(shortenUrlKey);
        return ResponseEntity.ok(shortenUrlInformationDto);
    }

    /** 모든 단축URL 조회 */
    @RequestMapping(value = "/shortenUrls", method = RequestMethod.GET)
    public ResponseEntity<List<ShortenUrlInformationDto>> getAllShortenUrlInformation() {
        List<ShortenUrlInformationDto> shortenUrlInformationDtoList = simpleShortenUrlService.getAllShortenUrlInformationDto();

        return ResponseEntity.ok(shortenUrlInformationDtoList);
    }

}
