package kr.co.shortenurlservice.presentation;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            // 요청을 CachedBodyHttpServletRequest로 래핑
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);

            String url = wrappedRequest.getRequestURI();
            String method = wrappedRequest.getMethod();
            String body = wrappedRequest.getReader().lines().reduce("", String::concat);

            //info 레벨은 가능한 비즈니스적인 로직에 사용
            log.trace("Incoming Request, URL={}, Method={}, Body={}", url, method, body);

            // 래핑된 요청 객체를 다음 필터 체인으로 전달
            chain.doFilter(wrappedRequest, response);
        } else {
            // HttpServletRequest가 아닌 경우 그대로 전달
            chain.doFilter(request, response);
        }

    }

        //오류 발생: java.lang.IllegalStateException: getReader() has already been called for this request
        //getReader는 Stream을 통해 읽는데 한번만 읽을 수 있기 때문에, body가 controller에서 한번 더 읽으려 하면 문제가 됨.
        //그래서 한번 캐싱을 해두고 그걸 사용해야함.
//    private String getRequestBody(HttpServletRequest request) {
//        try (BufferedReader reader = request.getReader()) {
//            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
//        } catch (IOException e) {
//            log.error("Failed to read request body", e);
//            return "Unable to read request body";
//        }
//    }
}
