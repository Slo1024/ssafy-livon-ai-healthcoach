package com.s406.livon.global.aop; // (패키지 경로 com.s406.livon으로 수정)

import com.s406.livon.domain.user.entity.User; // (User 엔티티 import)
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.util.log.LoggingService;
import com.s406.livon.global.web.response.code.ErrorReasonDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor; // [★추가]
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject; // (JSONObject import 추가)
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// --- [수정] 올바른 Spring Security import ---
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// --- [수정] ---

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor // [★추가] (LoggingService 주입을 위해)
public class LogAspect {

    // [★추가] 비동기 로깅 서비스 주입
    private final LoggingService loggingService;

    // --- [수정] Pointcut 경로 수정 ---
@Pointcut("execution(* com.s406.livon..*.*(..))" +
            " && !execution(* com.s406.livon..*Controller.*(..))" + // 컨트롤러 제외
            " && !execution(* com.s406.livon.global..*(..))")
    public void all() {
    }

    @Pointcut("execution(* com.s406.livon..*Controller.*(..)) && !execution(* com.s406.livon.global..*(..))")
    public void controller() {
    }
    // --- [수정 완료] ---


    @Around("all()")
    public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toShortString(); // (로그용)
        String loggerName = joinPoint.getSignature().getDeclaringTypeName(); // (DB 저장용)

        try {
            Object result = joinPoint.proceed();
            return result;

        } catch (Throwable e) {
            String dbLogMessage; // (DB 저장용 메시지)
            String requestInfo = ""; // (요청 정보 저장용)

            // --- [★추가] 현재 요청 정보 가져오기 ---
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                String httpMethod = request.getMethod();
                String requestUri = URLDecoder.decode(request.getRequestURI(), "UTF-8");
                requestInfo = String.format("at Request: [%s] %s | ", httpMethod, requestUri);
            } catch (Exception ex) {
                // HTTP 요청 컨텍스트가 아닐 경우 (예: @Scheduled)
                requestInfo = "at Non-HTTP Request | ";
            }
            // --- [추가 완료] ---

            // [★수정] 로그에 요청 정보 추가
            log.error("!!! {}에러 및 예외 {} !!!", requestInfo, signature);

            if (e instanceof GeneralException) {
                GeneralException ge = (GeneralException) e;
                ErrorReasonDTO reason = ge.getErrorReason();

                if (reason != null) {
                    // 1. 콘솔 로그
                    log.error("Exception: {}, Code: {}, Message: {}",
                            e.getClass().getSimpleName(), reason.getCode(), reason.getMessage());

                    // 2. DB 저장용 메시지 (요청 정보 포함)
                    dbLogMessage = String.format("%s!!! EXCEPTION !!! Code: %s, Message: %s",
                                                requestInfo, reason.getCode(), reason.getMessage());
                } else {
                    log.error("Exception: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
                    dbLogMessage = String.format("%s!!! EXCEPTION !!! Message: %s", requestInfo, e.getMessage());
                }
            } else {
                log.error("Exception: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
                dbLogMessage = String.format("%s!!! EXCEPTION !!! Exception: %s, Message: %s",
                                            requestInfo, e.getClass().getSimpleName(), e.getMessage());
            }

            // [★추가] 에러 로그를 DB에 비동기 저장
            loggingService.saveLog("ERROR", loggerName, dbLogMessage);

            throw e; // 예외를 다시 던져서 @RestControllerAdvice가 처리하도록 함

        } finally {
            log.info("----------------------------- 호출된 메소드 정보 -----------------------------");
            long end = System.currentTimeMillis();
            long timeinMs = end - start;
            log.info("{} | time = {}ms", signature, timeinMs);
        }
    }

    @Around("controller()")
    public Object loggingBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String controllerName = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        Map<String, Object> params = new HashMap<>();
        String username = "ANONYMOUS"; // (로그용)

        try {
            String decodedURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");

            params.put("controller", controllerName);
            params.put("method", methodName);
            params.put("params", getParams(request));
            params.put("log_time", System.currentTimeMillis());
            params.put("request_uri", decodedURI);
            params.put("http_method", request.getMethod());

            // --- [수정] 현재 사용자 정보 로깅 (ClassCastException 해결) ---
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof User) {
                    username = ((User) principal).getEmail();
                } else if (principal instanceof String && !principal.equals("anonymousUser")) {
                    username = (String) principal;
                } else {
                    username = authentication.getName();
                }
            }
            params.put("user", username);
            // --- [수정 완료] ---

        } catch (Exception e) {
            log.error("LoggerAspect error", e);
            // [★추가] AOP 자체에서 에러 발생 시, 이 로그도 DB에 저장
            loggingService.saveLog("ERROR", "LogAspect", "LoggerAspect error: " + e.getMessage());
        }

        // --- [수정] 콘솔 로그와 DB 로그 분리 ---
        String reqUri = (String) params.get("request_uri");
        String httpMethod = (String) params.get("http_method");
        String method = String.format("%s.%s", controllerName, methodName);
        String paramStr = params.get("params").toString();

        // 1. 콘솔 로그 (기존과 동일)
        log.info("----------------------------- 컨트롤러 요청 정보 -----------------------------");
        log.info("[{}] {}", httpMethod, reqUri);
        log.info("method: {}", method);
        log.info("user: {}", username);
        log.info("params: {}", paramStr);

        // 2. [★추가] DB 로그 (loggingService 호출)
        String dbLogMessage = String.format("[%s] %s | user: %s | params: %s",
                                        httpMethod, reqUri, username, paramStr);
        loggingService.saveLog("INFO", controllerName, dbLogMessage);
        // --- [수정 완료] ---

        Object result = joinPoint.proceed();

        return result;
    }

    private static JSONObject  getParams(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            String replaceParam = param.replaceAll("\\.", "-");
            jsonObject.put(replaceParam, request.getParameter(param));
        }
        return jsonObject;
    }
}

