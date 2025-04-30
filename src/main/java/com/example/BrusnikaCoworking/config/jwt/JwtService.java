//package com.example.BrusnikaCoworking.config.jwt;
//
//import com.example.BrusnikaCoworking.domain.user.UserEntity;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//public class JwtService {
//
//    //Извлечение имени пользователя из токена
//    public String extractUserName(String token, String jwtSigningKey) {
//        return extractClaim(token, Claims::getSubject, jwtSigningKey);
//    }
//
//    //Генерация токена
//    public String generateToken(UserDetails userDetails, String jwtSigningKey, Long jwtSigningTime) {
//        Map<String, Object> claims = new HashMap<>();
//        if (userDetails instanceof UserEntity customUserDetails) {
//            claims.put("id", customUserDetails.getId_user());
//            claims.put("username", customUserDetails.getUsername());
//            claims.put("role", customUserDetails.getAuthorities());
//        }
//        return generateToken(claims, userDetails, jwtSigningKey, jwtSigningTime);
//    }
//
//    //Проверка токена на валидность
//    public boolean isTokenValid(String token, UserDetails userDetails, String jwtSigningKey) {
//        final String userName = extractUserName(token, jwtSigningKey);
//        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token, jwtSigningKey);
//    }
//
//    //Извлечение данных из токена
//    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers, String jwtSigningKey) {
//        final Claims claims = extractAllClaims(token, jwtSigningKey);
//        return claimsResolvers.apply(claims);
//    }
//
//    //Генерация токена
//    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails,
//                                 String jwtSigningKey, Long jwtSigningTime) {
//        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + jwtSigningTime))
//                .signWith(getSigningKey(jwtSigningKey), SignatureAlgorithm.HS256).compact();
//    }
//
//    //Проверка токена на просроченность
//    private boolean isTokenExpired(String token, String jwtSigningKey) {
//        return extractExpiration(token, jwtSigningKey).before(new Date());
//    }
//
//    //Извлечение даты истечения токена
//    private Date extractExpiration(String token, String jwtSigningKey) {
//        return extractClaim(token, Claims::getExpiration, jwtSigningKey);
//    }
//
//    //Извлечение всех данных из токена
//    private Claims extractAllClaims(String token, String jwtSigningKey) {
//        return Jwts.parser().setSigningKey(getSigningKey(jwtSigningKey)).parseClaimsJws(token)
//                .getBody();
//    }
//
//     //Получение ключа для подписи токена
//    private Key getSigningKey(String jwtSigningKey) {
//        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//}
//
