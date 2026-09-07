package com.laundry.security;

import com.laundry.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Guards /api/admin/** (must be ADMIN) and /api/customer/** (must be CUSTOMER).
 * Reads "Authorization: Bearer <token>" and looks it up in TokenStore.
 * On success, stashes the resolved phone/role on the request as attributes
 * so controllers can read "who is calling" without re-parsing the header.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final TokenStore tokenStore;

    public AuthFilter(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Always allow CORS preflight requests through. The browser sends an
        // OPTIONS request before requests that contain the Authorization header.
        // If AuthFilter rejects the preflight, Spring cannot add the CORS headers
        // and the browser blocks the actual request.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Only guard admin/customer APIs; login endpoint and H2 console stay open.
        return !(path.startsWith("/api/admin") || path.startsWith("/api/customer"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
        TokenStore.Session session = token != null ? tokenStore.resolve(token) : null;

        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Not logged in. Please log in again.\"}");
            return;
        }

        boolean wantsAdmin = request.getRequestURI().startsWith("/api/admin");
        boolean isAdmin = session.role() == Role.ADMIN;
        if (wantsAdmin != isAdmin) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Not authorized for this area.\"}");
            return;
        }

        request.setAttribute("phone", session.phone());
        request.setAttribute("role", session.role());
        request.setAttribute("name", session.name());

        filterChain.doFilter(request, response);
    }
}