package com.apisecurity.capture;

import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface UserIdResolver {
    String resolve(HttpServletRequest request);
}
