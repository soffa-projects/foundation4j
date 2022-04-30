package dev.soffa.foundation.commons;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.io.InputStream;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class TokenUtil {

    private static final Logger LOG = Logger.get(TokenUtil.class);
    private static final int JWT_PARTS_LENGTH = 3;

    private TokenUtil() {
    }

    public static boolean isWellFormedJwt(final String jwt) {
        // Token should look like a well-formed JWT before proceeding
        String[] jwtSplitted = jwt.split("\\.");
        if (jwtSplitted.length != JWT_PARTS_LENGTH) {
            LOG.warn("Received token *******%s is not a Well-formed JWT", TextUtil.takeLast(jwt, 5));
            return false;
        }
        return true;
    }

    @SneakyThrows
    public static String createJwt(final String issuer, final String secretKey,
                                   final String subject, final Map<String, Object> claims,
                                   final int timeToLiveInMinutes) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        final Date issuedAt = new Date();
        JWTCreator.Builder builder = JWT.create()
            .withIssuedAt(issuedAt)
            .withSubject(subject)
            .withExpiresAt(DateUtil.plusMinutes(issuedAt, timeToLiveInMinutes))
            .withIssuer(issuer);
        if (claims != null) {
            for (Map.Entry<String, Object> claim : claims.entrySet()) {
                populateClaims(builder, claim.getKey(), claim.getValue());
            }
        }
        return builder.sign(algorithm);
    }

    @SuppressWarnings("unchecked")
    private static void populateClaims(JWTCreator.Builder builder, String name, Object value) {
        if (value == null) {
            LOG.warn("Skipping empy claim: %s", name);
            return;
        }
        LOG.debug("Populating claim %s=%s", name, value);
        if (value instanceof Integer) {
            builder.withClaim(name, (Integer) value);
        } else if (value instanceof Double) {
            builder.withClaim(name, (Double) value);
        } else if (value instanceof Long) {
            builder.withClaim(name, (Long) value);
        } else if (value instanceof Boolean) {
            builder.withClaim(name, (Boolean) value);
        } else if (value instanceof Date) {
            builder.withClaim(name, (Date) value);
        } else if (value instanceof String) {
            builder.withClaim(name, value.toString());
        } else if (value instanceof List<?>) {
            builder.withClaim(name, (List<?>) value);
        } else if (value instanceof Map<?, ?>) {
            builder.withClaim(name, (Map<String, ?>) value);
        } else {
            throw new TechnicalException("Claim type is not supported: %s", value.getClass());
        }
    }

    @SneakyThrows
    public static String fromJwks(final InputStream jwkSource, final String issuer, final String subject, final Map<String, Object> claims, Duration ttl) {
        String jwkString = IOUtil.toString(jwkSource).orElseThrow(() -> new TechnicalException("INVALID_JWK_SOURCE"));
        return fromJwks(jwkString, issuer, subject, claims, ttl);
    }

    @SneakyThrows
    public static String fromJwks(final String jwkString, final String issuer, final String subject, final Map<String, Object> claims, Duration ttl) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Using JWK: %s", jwkString);
        }
        JSONObject json = new JSONObject(jwkString);
        if (json.has("keys")) {
            json = json.getJSONArray("keys").getJSONObject(0);
        }
        JWK jwk = JWK.parse(new net.minidev.json.JSONObject(json.toMap()));
        RSAKey rsaJWK = jwk.toRSAKey();
        JWSSigner signer = new RSASSASigner(rsaJWK);
        Date issuedAt = new Date();
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder().subject(subject).issuer(issuer)
            .issueTime(issuedAt)
            .expirationTime(DateUtil.plusSeconds(issuedAt, (int) ttl.getSeconds()));
        if (claims != null) {
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsSetBuilder.claim(entry.getKey(), entry.getValue());
            }
        }
        JWTClaimsSet claimsSet = claimsSetBuilder.build();
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).build();
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

}
