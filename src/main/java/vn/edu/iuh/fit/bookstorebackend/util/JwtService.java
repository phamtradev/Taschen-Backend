package vn.edu.iuh.fit.bookstorebackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import vn.edu.iuh.fit.bookstorebackend.model.RefreshToken;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.RefreshTokenRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.secret:replace-with-secure-secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-seconds:900}")
    private long accessTokenExpirationSeconds; 

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }       

    //note: cần xóa toàn bộ token cũ của người dùng để tránh trùng lặp
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenExpirationSeconds);

        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("roles", user.getRoles() == null ? null :
                        user.getRoles().stream()
                        .map(r -> r.getCode())
                        .collect(Collectors.toList()))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm());
    }


    //note: throw lỗi ra
    //check tồn tại trong db
    //check không revoke
    public DecodedJWT verify(String token) {
        DecodedJWT decoded = JWT.require(algorithm()).build().verify(token);

        //check tồn tại trong db
        String username = decoded.getSubject();
        if (username != null) {
            Optional<User> maybeUser = userRepository.findByUsername(username);
            if (maybeUser.isPresent()) {
                User user = maybeUser.get();
                Set<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
                boolean hasValid = tokens != null && tokens.stream().anyMatch(t ->
                        !t.isRevoked() && t.getExpiresAt() != null && t.getExpiresAt().isAfter(Instant.now()));
                if (!hasValid) {
                    throw new BadCredentialsException("Session invalid - login again");
                }
            }
        }

        return decoded;
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirationSeconds;
    }
}


