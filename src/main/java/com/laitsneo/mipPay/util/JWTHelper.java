package com.laitsneo.mipPay.util;

import com.laitsneo.mipPay.repository.Admin.UserRepository;
import com.laitsneo.mipPay.repository.Client.ClientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTHelper {

    @Autowired
    private UserRepository adminRepository;
    @Autowired
    private ClientRepository clientRepository;

    public static final long JWT_TOKEN_VALIDITY = 20 * 60 * 60;

    //    public static final long JWT_TOKEN_VALIDITY =  60;
    private String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";


    private Key createSecretKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        System.out.println("token: "+token);
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve id from token
    public String getIdFromToken (String token) {
        return getClaimFromToken (token, Claims::getId);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) createSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    public String generateToken(UserDetails userDetails, String clientId){
        Map<String, Object> claims = new HashMap<>();
//        Optional<Admin> user = Optional.ofNullable(this.adminRepository.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new CustomBadCredentialsException("Username or Password is not valid !!")));
            return doGenerateToken(claims,userDetails.getUsername(),clientId);
    }


    private String doGenerateToken(Map<String, Object> claims, String subject, String id) {
        System.out.println("validity: "+JWT_TOKEN_VALIDITY);
        return Jwts.builder()
                .claims(claims)
                .id(id)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(createSecretKey())
                .compact();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
