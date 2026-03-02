package vn.edu.iuh.fit.bookstorebackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.bookstorebackend.common.HttpMethod;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.PermissionService;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionFilter extends OncePerRequestFilter {

    private final PermissionService permissionService;
    private final UserRepository userRepository;

    // Các endpoint không cần kiểm tra permission (đã được permitAll trong Security)
    private static final String[] SKIP_FILTER_PATHS = {
            "/api/auth/",
            "/api/promotions/validate/",
            "/api/batches/",
            "/api/variants/",
            "/api/book-variants/",
            "/actuator/"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip filter cho public endpoints
        if (shouldSkipFilter(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Nếu chưa authenticate, để Spring Security xử lý
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = auth.getName();
        try {
            User user = userRepository.findByEmailWithRoles(email)
                    .orElse(null);

            if (user != null && user.getRoles() != null) {
                Set<Long> roleIds = user.getRoles().stream()
                        .map(role -> role.getId())
                        .collect(Collectors.toSet());

                HttpMethod httpMethod = HttpMethod.valueOf(method);
                boolean hasPermission = permissionService.hasPermission(roleIds, httpMethod, path);

                if (!hasPermission) {
                    log.warn("Access denied for user {} on {} {}", email, method, path);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: insufficient permission");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error checking permission for user {} on {} {}: {}", email, method, path, e.getMessage());
            // Nếu lỗi, vẫn cho đi tiếp để Spring Security xử lý
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(String path) {
        for (String skipPath : SKIP_FILTER_PATHS) {
            if (path.startsWith(skipPath)) {
                return true;
            }
        }
        return false;
    }
}
