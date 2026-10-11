package com.example.store.security;

import com.example.store.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            jwt = authorizationHeader.substring(BEARER_PREFIX.length());
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException exception) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has expired");
                return;
            } catch (Exception exception) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid token");
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                List<GrantedAuthority> authorities = jwtUtil.extractAuthorities(jwt);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                }
            } catch (Exception exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
