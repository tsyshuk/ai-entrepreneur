package org.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.config.JwtProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;

        // Берём секрет из настроек (app.security.jwt.secret)
        String secret = props.getSecret();

        // Защита от ситуации, когда секрет не задан вообще
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. " +
                            "Проверь, что свойство 'app.security.jwt.secret' задано " +
                            "в application-*.yml или через переменную окружения APP_SECURITY_JWT_SECRET."
            );
        }

        // Декодируем Base64 в байты и строим ключ для HS256
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Выпускаем access-token для пользователя (subject = email).
     */
    public String generateAccessToken(UserDetails user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.getExpiresIn());

        return Jwts.builder()
                .subject(user.getUsername())          // email как "subject"
                .issuer(props.getIssuer())            // кто выдал
                .issuedAt(Date.from(now))             // когда выдали
                .expiration(Date.from(exp))           // когда истечёт
                .signWith(key, Jwts.SIG.HS256)        // подпись HS256
                .compact();
    }

    /**
     * Валидируем токен и возвращаем subject (email), если подпись/срок ок.
     */
    public String validateAndGetSubject(String token) {
        // Если подпись неверна / токен истёк — бросит исключение
        JwtParser parser = Jwts.parser().verifyWith(key).build();
        Jws<Claims> jws = parser.parseSignedClaims(token);

        String issuer = jws.getPayload().getIssuer();
        if (props.getIssuer() != null && !props.getIssuer().equals(issuer)) {
            throw new JwtException("Invalid issuer");
        }

        return jws.getPayload().getSubject();
    }
}