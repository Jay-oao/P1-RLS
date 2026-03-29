package com.p1rls.rls.utils;

import jakarta.servlet.http.HttpServletRequest;

 public class ExtractIP {
    public static String extractIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");

        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0];
        }

        return request.getRemoteAddr();
    }
}
