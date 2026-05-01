package vn.edu.iuh.fit.bookstorebackend.shared.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import vn.edu.iuh.fit.bookstorebackend.shared.common.HttpMethod;
import vn.edu.iuh.fit.bookstorebackend.user.model.Role;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;
import vn.edu.iuh.fit.bookstorebackend.user.service.PermissionService;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionFilter extends OncePerRequestFilter {

    private final PermissionService permissionService;

    @PersistenceContext
    private EntityManager entityManager;

    // Các endpoint không cần kiểm tra permission (đã được permitAll trong Security)
    private static final String[] SKIP_FILTER_PATHS = {
            "/api/auth/",
            "/api/promotions/validate/",
            "/api/batches/",
            "/api/variants/",
            "/api/book-variants/",
            "/api/banners/",
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
            // Chỉ fetch User để lấy role IDs, KHÔNG fetch permissions
            // Permissions sẽ được load bên trong service qua RoleRepository.findByIdIn
            // (nằm trong @Transactional(readOnly = true) nên entities luôn attached)
            Set<Long> roleIds = entityManager.createQuery(
                    "SELECT r.id FROM User u JOIN u.roles r WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toSet());

            if (!roleIds.isEmpty()) {
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
