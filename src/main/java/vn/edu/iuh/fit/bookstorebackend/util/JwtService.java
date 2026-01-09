package vn.edu.iuh.fit.bookstorebackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import vn.edu.iuh.fit.bookstorebackend.model.User;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

public class JwtService {

    
    private static final String SECRET = "replace-with-secure-secret";
    private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 15 * 60; // 15 min

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS);

        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("roles", user.getRoles() == null ? null :
                        user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toList()))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);
    }

    public DecodedJWT verify(String token) {
        return JWT.require(algorithm).build().verify(token);
    }

    public long getAccessTokenExpirySeconds() {
        return ACCESS_TOKEN_EXPIRATION_SECONDS;
    }
}


