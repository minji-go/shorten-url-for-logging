package kr.co.shortenurlservice.application;

import kr.co.shortenurlservice.domain.LackOfShortenUrlKeyException;
import kr.co.shortenurlservice.domain.NotFoundShortenUrlException;
import kr.co.shortenurlservice.domain.ShortenUrl;
import kr.co.shortenurlservice.domain.ShortenUrlRepository;
import kr.co.shortenurlservice.presentation.ShortenUrlCreateRequestDto;
import kr.co.shortenurlservice.presentation.ShortenUrlCreateResponseDto;
import kr.co.shortenurlservice.presentation.ShortenUrlInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SimpleShortenUrlService {

    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    SimpleShortenUrlService(ShortenUrlRepository shortenUrlRepository) {
        this.shortenUrlRepository = shortenUrlRepository;
    }

    public ShortenUrlCreateResponseDto generateShortenUrl(ShortenUrlCreateRequestDto shortenUrlCreateRequestDto) {
        //3. 모든 로그를 남기면 불필요하게 로그 파일 사이즈가 커져서 활용하기 어려움.
        //log.info("generateShortenUrl {}", shortenUrlCreateRequestDto.getOriginalUrl());
        String originalUrl = shortenUrlCreateRequestDto.getOriginalUrl();
        String shortenUrlKey = getUniqueShortenUrlKey();
        //log.info("getUniqueShortenUrlKey {}", shortenUrlKey);

        ShortenUrl shortenUrl = new ShortenUrl(originalUrl, shortenUrlKey);
        shortenUrlRepository.saveShortenUrl(shortenUrl);

        ShortenUrlCreateResponseDto shortenUrlCreateResponseDto = new ShortenUrlCreateResponseDto(shortenUrl);
        return shortenUrlCreateResponseDto;
    }

    public String getOriginalUrlByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if(null == shortenUrl) {
            //4. Runtime 상속받은 Unchecked Exception
            //   높은 확률로 사용자가 잘못 요청했을 것이기 때문에, 사용자에게 에러를 그대로 알려줘야 한다.
            //5. Exception 상속받은 Checked Exception 이었다면?
            //   throws로 계속 던져주어야 GlobalExceptionHandler에서 처리할 수 있다.
            //   Checked Exception이 유리한 경우는 예외가 발생한 곳에서 처리할 수 있는 경우이고, 대부분은 Unchecked가 유리하다.
            throw new NotFoundShortenUrlException("단축 URL을 찾지 못했습니다. shortenUrlKey=" + shortenUrlKey);
        }

        shortenUrl.increaseRedirectCount();
        shortenUrlRepository.saveShortenUrl(shortenUrl);

        String originalUrl = shortenUrl.getOriginalUrl();

        return originalUrl;
    }

    public ShortenUrlInformationDto getShortenUrlInformationByShortenUrlKey(String shortenUrlKey) {
        ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

        if(null == shortenUrl)
            throw new NotFoundShortenUrlException("단축 URL을 찾지 못했습니다. shortenUrlKey=" + shortenUrlKey);

        ShortenUrlInformationDto shortenUrlInformationDto = new ShortenUrlInformationDto(shortenUrl);

        return shortenUrlInformationDto;
    }

    public List<ShortenUrlInformationDto> getAllShortenUrlInformationDto() {
        List<ShortenUrl> shortenUrls = shortenUrlRepository.findAll();

        return shortenUrls
                .stream()
                .map(shortenUrl -> new ShortenUrlInformationDto(shortenUrl))
                .toList();
    }

    private String getUniqueShortenUrlKey() {
        final int MAX_RETRY_COUNT = 5;
        int count = 0;

        while(count++ < MAX_RETRY_COUNT) {
            String shortenUrlKey = ShortenUrl.generateShortenUrlKey();
            ShortenUrl shortenUrl = shortenUrlRepository.findShortenUrlByShortenUrlKey(shortenUrlKey);

            if(null == shortenUrl)
                return shortenUrlKey;
        }

        throw new LackOfShortenUrlKeyException();
    }

}
