package com.codegym.salesmanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        String redirectURL = switch (getURIType(requestURI)) {
            case "REVIEW" -> "/login?error=review_required";
            case "CART" -> "/login?error=cart_required";
            case "ORDERS" -> "/login?error=order_required";
            case "NOTIFICATIONS" -> "/login?error=notification_required";
            case "PROFILE" -> "/login?error=profile_required";
            case "OAUTH2" -> "/login?oauth2=true";
            default -> "/login?error=login_required";
        };

        // Lưu lại URI hiện tại để chuyển hướng sau khi đăng nhập
        HttpSession session = request.getSession();
        session.setAttribute("redirect_uri", requestURI);

        response.sendRedirect(redirectURL);
    }

    private String getURIType(String requestURI) {
        if (requestURI.contains("/review")) {
            return "REVIEW";
        } else if (requestURI.contains("/cart")) {
            return "CART";
        } else if (requestURI.contains("/orders")) {
            return "ORDERS";
        } else if (requestURI.contains("/notifications")) {
            return "NOTIFICATIONS";
        } else if (requestURI.contains("/profile")) {
            return "PROFILE";
        } else if (requestURI.startsWith("/oauth2/")) {
            return "OAUTH2";
        } else {
            return "DEFAULT";
        }
    }

}
