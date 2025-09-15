package gr.aueb.cf.eduapp.security;

import gr.aueb.cf.eduapp.authentication.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

//        // ADDED: Skip JWT processing for multipart requests to prevent 415 errors
//        String contentType = request.getContentType();
//        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
//            log.debug("Skipping JWT filter for multipart request: {}", request.getServletPath());
//            filterChain.doFilter(request, response);
//            return;
//        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7).trim();
        try {
            username = jwtService.extractSubject(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new BadCredentialsException("Invalid Token");
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (ExpiredJwtException e) {
            // triggers AuthenticationEntryPoint 401 error
            throw new AuthenticationCredentialsNotFoundException("Expired token", e);
        } catch (JwtException | IllegalArgumentException e) {
            // triggers AuthenticationEntryPoint 401 error
            throw new BadCredentialsException("Invalid Token");
        } catch (Exception e) {
            // triggers AccessDenied 403
            throw new AccessDeniedException("Token validation failed");
        }

        filterChain.doFilter(request, response);
    }

    // ADDED: Optional - shouldNotFilter method for additional control
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        String method = request.getMethod();
//
//        // Skip JWT filter for authentication endpoint
//        boolean isAuthEndpoint = "/api/auth/authenticate".equals(path);
//
//        if (isAuthEndpoint) {
//            log.debug("Skipping JWT filter for authentication endpoint");
//            return true;
//        }
//
//        return false;
//    }
}