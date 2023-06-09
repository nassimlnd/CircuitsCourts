package com.lifat.CircuitsCourtsApi.security.jwt;

import java.util.Date;

import com.lifat.CircuitsCourtsApi.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${com.lifat.circuitscourtsapi.jwtSecret: ad3e3c0ef0c8d0f70f1e4a9b4b892978e87f4a2d82b9e4b23e461b139a8f6c7018d749bb829e3416b6e3502aeb166bc914a3d206725edf6d9b4c85df8b0b4f7}")
    private String jwtSecret;

    @Value("${com.lifat.circuitscourtsapi.jwtExpirationMs: 86400000}")
    private int jwtExpirationMs;

    /**
     * Crée un token valide à partir d'une clée secrete et des données de l'utilisateur.
     * JWT valide 24 H.
     * @param authentication
     * @return le JWT
     */
    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Récupère le nom de l'utilisateur dans un token
     * @param token
     * @return le nom de l'utilisateur
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * @param authToken
     * @return  boolean qui atteste de la validité du token
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}