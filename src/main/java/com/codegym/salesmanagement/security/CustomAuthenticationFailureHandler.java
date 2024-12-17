package com.codegym.salesmanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Nếu lỗi là tài khoản chưa kích hoạt
        if (exception instanceof DisabledException) {
            response.sendRedirect("/login?error=notActivated");
        }
        // Kiểm tra nếu tài khoản bị khóa
        else if (exception instanceof LockedException) {
            response.sendRedirect("/login?error=locked");
        } else if (exception instanceof InternalAuthenticationServiceException) {
            if (exception.getCause() instanceof DisabledException) {
                response.sendRedirect("/login?error=notActivated");
            } else if (exception.getCause() instanceof LockedException) {
                response.sendRedirect("/login?error=locked");
            } else {
                response.sendRedirect("/login?error=true");
            }
        } else {
            // Lỗi khác (sai tên đăng nhập hoặc mật khẩu)
            response.sendRedirect("/login?error=true");
        }
    }
}
