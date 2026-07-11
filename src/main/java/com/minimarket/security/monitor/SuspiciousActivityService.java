package com.minimarket.security.monitor;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SuspiciousActivityService {

    private static final Logger log = LoggerFactory.getLogger(SuspiciousActivityService.class);

    private final ConcurrentHashMap<String, List<Long>> failedLoginTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Long>> requestTimestampsByIp = new ConcurrentHashMap<>();
    private final int FAILED_LOGIN_THRESHOLD = 5;
    private final int REQUEST_THRESHOLD = 200;
    private final long WINDOW_MS = 15 * 60 * 1000L;

    private String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return xf != null && !xf.isBlank() ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }

    private void pruneOld(List<Long> list) {
        long cutoff = Instant.now().toEpochMilli() - WINDOW_MS;
        list.removeIf(t -> t < cutoff);
    }

    public void recordFailedLogin(HttpServletRequest req, String username) {
        String key = "FAILED_LOGIN:" + (username == null ? clientIp(req) : username + "@" + clientIp(req));
        List<Long> list = failedLoginTimestamps.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            log.warn("SuspiciousActivity: failed login (user={}, ip={}, count={})", username, clientIp(req), count);
            if (count >= FAILED_LOGIN_THRESHOLD) {
                log.warn("SuspiciousActivity: threshold reached for failed logins (user/ip={}): {}", key, count);
            }
        }
    }

    public void recordInvalidJwt(HttpServletRequest req, String token, Exception ex) {
        String ip = clientIp(req);
        log.warn("SuspiciousActivity: invalid JWT from ip={} path={} reason={}", ip, req.getRequestURI(),
                ex == null ? "invalid/expired" : ex.getMessage());
    }

    public void recordRequest(HttpServletRequest req) {
        String ip = clientIp(req);
        List<Long> list = requestTimestampsByIp.computeIfAbsent(ip, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            if (count % 50 == 0) {
                log.info("SuspiciousActivity: request rate ip={} count_last_15m={}", ip, count);
            }
            if (count >= REQUEST_THRESHOLD) {
                log.warn("SuspiciousActivity: high request rate detected ip={} count_last_15m={}", ip, count);
            }
        }
    }
}
