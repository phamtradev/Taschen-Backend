package vn.edu.iuh.fit.bookstorebackend.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.bookstorebackend.util.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        logger.debug("Incoming request {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        logger.debug("Authorization header present: {}", header != null);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                logger.debug("Verifying access token");
                DecodedJWT decoded = jwtService.verify(token);
                String email = decoded.getSubject();
                List<String> roles = decoded.getClaim("roles").isNull() ? List.of() : decoded.getClaim("roles").asList(String.class);
                logger.debug("Token valid for subject='{}', roles={}", email, roles);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (AuthenticationException ex) {
                logger.warn("Authentication exception while verifying token: {}", ex.getMessage());
                // authentication failure -> return 401
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                return;
            } catch (Exception ex) {
                logger.error("Unexpected error in JwtAuthenticationFilter: {}", ex.getMessage(), ex);
                // other errors -> clear context and continue (will likely be rejected)
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}


