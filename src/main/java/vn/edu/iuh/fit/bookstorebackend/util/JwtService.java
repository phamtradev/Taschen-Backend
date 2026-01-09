package vn.edu.iuh.fit.bookstorebackend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.model.User;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${app.jwt.secret:replace-with-secure-secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-seconds:900}")
    private long accessTokenExpirationSeconds; // default 15 minutes

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenExpirationSeconds);

        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId())
                .withClaim("roles", user.getRoles() == null ? null :
                        user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toList()))
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm());
    }

    public DecodedJWT verify(String token) {
        return JWT.require(algorithm()).build().verify(token);
    }

    public long getAccessTokenExpirySeconds() {
        return accessTokenExpirationSeconds;
    }
}


