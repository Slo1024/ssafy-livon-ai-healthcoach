package com.s406.livon.global.aop;

import com.s406.livon.domain.user.entity.User;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.util.log.LoggingService;
import com.s406.livon.global.web.response.code.ErrorReasonDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LogAspect {

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

            // [★★★★★ 여기 로그가 추가되었습니다 ★★★★★]
            // LogAspect가 잡은 '진짜 원본 오류(A)'를 확인하기 위한 로그입니다.
            // (이 로그는 콘솔에만 보이고 DB에는 저장되지 않습니다)
            log.error("==================================================================");
            log.error("!!! [LogAspect] 원본 예외(A) 발견 !!!");
            log.error("!!! 원본 예외 타입: {}", e.getClass().getName());
            log.error("!!! 원본 예외 메시지: {}", e.getMessage());
            log.error("!!! 스택 트레이스: ", e); // <--- 이게 가장 중요합니다. 스택 트레이스 전체를 출력
            log.error("==================================================================");
            // [★★★★★ 로그 추가 끝 ★★★★★]


            String dbLogMessage; // (DB 저장용 메시지)
            String requestInfo = ""; // (요청 정보 저장용)

            // --- [★수정] 요청 정보 형식 변경 (at Request: ...) ---
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String httpMethod = request.getMethod();
                    String requestUri = URLDecoder.decode(request.getRequestURI(), "UTF-8");
                    requestInfo = String.format("at Request: [%s] %s | ", httpMethod, requestUri);
                } else {
                     requestInfo = "at Non-HTTP Request (e.g., WebSocket) | ";
                }
            } catch (Exception ex) {
                // HTTP 요청 컨텍스트가 아닐 경우 (예: @Scheduled)
                requestInfo = "at Non-HTTP Request | ";
            }
            // --- [수정 완료] ---

            // 콘솔에는 자세한 에러 로그를 남깁니다.
            log.error("!!! {}오류 발생: {} !!!", requestInfo, signature);

            // [★★★★★ DB 저장용 에러 로그 메시지 형식 수정 ★★★★★]
            // (요청하신 형식으로 변경)
            if (e instanceof GeneralException) {
                GeneralException ge = (GeneralException) e;
                ErrorReasonDTO reason = ge.getErrorReason();

                if (reason != null) {
                    log.error("▶ 예외: {}, 코드: {}, 메시지: {}",
                            e.getClass().getSimpleName(), reason.getCode(), reason.getMessage());
                    // DB 저장용 메시지
                    dbLogMessage = String.format("%s!!! EXCEPTION !!! Exception: %s, Code: %s, Message: %s",
                                                requestInfo, // "at Request: [...] | "
                                                e.getClass().getSimpleName(), reason.getCode(), reason.getMessage());
                } else {
                    log.error("▶ 예외: {}, 메시지: {}", e.getClass().getSimpleName(), e.getMessage());
                    dbLogMessage = String.format("%s!!! EXCEPTION !!! Exception: %s, Message: %s",
                                                requestInfo,
                                                e.getClass().getSimpleName(), e.getMessage());
                }
            } else {
                log.error("▶ 예외: {}, 메시지: {}", e.getClass().getSimpleName(), e.getMessage());
                dbLogMessage = String.format("%s!!! EXCEPTION !!! Exception: %s, Message: %s",
                                            requestInfo,
                                            e.getClass().getSimpleName(), e.getMessage());
            }

            // DB에 저장될 메시지 길이가 너무 길면 자릅니다. (예: 255자)
            if (dbLogMessage.length() > 250) {
                dbLogMessage = dbLogMessage.substring(0, 250) + "...";
            }
            // [★★★★★ 수정 완료 ★★★★★]


            loggingService.saveLog("ERROR", loggerName, dbLogMessage);
            throw e;
        } finally {
            log.info("----------------------------- 메소드 실행 완료 -----------------------------");
            long end = System.currentTimeMillis();
            long timeinMs = end - start;
            log.info("▶ 실행: {} | 소요시간 = {}ms", signature, timeinMs);
        }
    }

    // [★★★★★ 여기는 '웹소켓'도 처리하도록 수정된 버전입니다 ★★★★★]
    @Around("controller()")
    public Object loggingBefore(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. HTTP 요청인지 확인하기 위해 속성을 가져옵니다.
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String controllerName = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        String username = "UNKNOWN"; // (로그용)
        String dbLogMessage; // (DB 저장용)

        // 2. attributes가 null이 아닌지 확인 (null이면 웹소켓, null이 아니면 HTTP)
        if (attributes != null) {
            // --- 3. HTTP 요청일 경우 (기존 로직) ---
            HttpServletRequest request = attributes.getRequest();
            Map<String, Object> params = new HashMap<>();

            try {
                String decodedURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
                params.put("controller", controllerName);
                params.put("method", methodName);
                params.put("params", getParams(request));
                params.put("log_time", System.currentTimeMillis());
                params.put("request_uri", decodedURI);
                params.put("http_method", request.getMethod());

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

                // 콘솔 로그 (HTTP)
                log.info("----------------------------- 컨트롤러 요청 (HTTP) -----------------------------");
                log.info("▶ [HTTP 요청] {} {}", params.get("http_method"), params.get("request_uri"));
                log.info("▶ 컨트롤러: {}.{}", controllerName, methodName);
                log.info("▶ 사용자: {}", username);
                log.info("▶ 파라미터: {}", params.get("params").toString());

                // DB 로그 (HTTP)
                dbLogMessage = String.format("[%s] %s | 사용자: %s | 파라미터: %s",
                                        params.get("http_method"), params.get("request_uri"), username, params.get("params").toString());

                // [★★★★★ 추가 ★★★★★] DB 저장용 로그 길이 제한
                if (dbLogMessage.length() > 250) {
                    dbLogMessage = dbLogMessage.substring(0, 250) + "...";
                }

                loggingService.saveLog("INFO", controllerName, dbLogMessage);

            } catch (Exception e) {
                log.error("▶ LogAspect (HTTP) 오류", e);
                loggingService.saveLog("ERROR", "LogAspect", "LoggerAspect (HTTP) 오류: " + e.getMessage());
            }

        } else {
            // --- 4. 웹소켓 요청일 경우 (새 로직) ---
            try {
                // 웹소켓에서는 SecurityContextHolder에서 사용자 정보를 가져옵니다.
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

                // [★★★★★ 여기가 수정되었습니다 ★★★★★]
                // 웹소켓 페이로드(Payload)를 로깅합니다.
                String payloadClassName = "[페이로드 없음]";
                try {
                    // Authentication 객체를 제외한 첫 번째 인자(아마도 DTO)의 클래스 이름을 찾습니다.
                    Object payloadArg = Arrays.stream(joinPoint.getArgs())
                                              .filter(arg -> !(arg instanceof Authentication))
                                              .findFirst()
                                              .orElse(null);
                    if (payloadArg != null) {
                        payloadClassName = payloadArg.getClass().getSimpleName();
                    }
                } catch (Exception e) {
                    payloadClassName = "[페이로드 파싱 오류]";
                }

                // 콘솔 로그 (WebSocket) - 여기는 길어도 괜찮습니다.
                log.info("----------------------------- 컨트롤러 요청 (WebSocket) -----------------------------");
                log.info("▶ [웹소켓 요청] {}.{}", controllerName, methodName);
                log.info("▶ 사용자: {}", username);
                log.info("▶ 페이로드 (전체): {}", Arrays.toString(joinPoint.getArgs())); // @Payload로 받은 인자들

                // DB 로그 (WebSocket) - [★★★★★ 여기를 짧게 수정했습니다 ★★★★★]
                // DB에 저장되는 로그는 짧게 만듭니다. (payload -> payload class)
                dbLogMessage = String.format("[웹소켓] %s.%s | 사용자: %s | 페이로드 클래스: %s",
                                        controllerName, methodName, username, payloadClassName);

                // [★★★★★ 추가 ★★★★★] DB 저장용 로그 길이 제한
                if (dbLogMessage.length() > 250) {
                    dbLogMessage = dbLogMessage.substring(0, 250) + "...";
                }

                loggingService.saveLog("INFO", controllerName, dbLogMessage);

            } catch (Exception e) {
                 log.error("▶ LogAspect (WebSocket) 오류", e);
                 loggingService.saveLog("ERROR", "LogAspect", "LoggerAspect (WebSocket) 오류: " + e.getMessage());
            }
        }

        // 5. if/else 블록 바깥에서 joinPoint.proceed()를 실행합니다.
        Object result = joinPoint.proceed();
        return result;
    }

    private static JSONObject getParams(HttpServletRequest request) {
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