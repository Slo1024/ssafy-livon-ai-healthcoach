package com.s406.livon.global.security.jwt;


import com.s406.livon.domain.user.dto.JwtToken;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.handler.TokenHandler;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    //테스트용
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 ;
//    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L *100000;
    private final UserRepository userRepository; // UserRepository 주입
//    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L ;

    // application.yml에서 secret 값 가져와서 key에 저장
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.userRepository = userRepository; // 주입받은 리포지토리 할당
    }

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드
    public JwtToken generateToken(Authentication authentication) {

        com.s406.livon.domain.user.entity.User user =
                (com.s406.livon.domain.user.entity.User) authentication.getPrincipal();

        List<Role> roles = user.getRoles(); // User 엔티티에서 직접 Role 가져오기

        // JWT에 들어갈 권한 문자열 (예: "ROLE_USER,ROLE_ADMIN")
        String authorities = roles.stream()
                .map(Role::getRoleName) // getRoleName() 사용 권장
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                // ★★★ 수정된 부분: Subject에 이메일 대신 User ID(UUID) 저장
                .setSubject(user.getId().toString())
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(roles)
                .refreshTokenExpirationTime(REFRESH_TOKEN_EXPIRE_TIME)
                .build();
    }

    // Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // Jwt 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new UserHandler(ErrorStatus._UNAUTHORIZED);
        }

        // ★★★ 수정된 부분: subject에서 UUID를 파싱
        UUID userId = UUID.fromString(claims.getSubject());

        // ★★★ 수정된 부분: UUID로 DB에서 실제 User 엔티티 조회
        UserDetails principal = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 클레임에서 권한 정보 가져오기 (이 부분은 UserDetails에서 직접 가져와도 됩니다)
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        // ★★★ 수정된 부분: principal에 DB에서 조회한 실제 User 엔티티를 넣음
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (SignatureException e){
            throw new TokenHandler(ErrorStatus.TOKEN_SIGNATURE_INVALID);
        } catch (SecurityException | MalformedJwtException e) {
            throw new TokenHandler(ErrorStatus.TOKEN_SIGNATURE_INVALID);
        } catch (ExpiredJwtException e) {
            throw new TokenHandler(ErrorStatus.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new TokenHandler(ErrorStatus.UNSUPPORTED_JWT_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new TokenHandler(ErrorStatus.INVALID_JWT_TOKEN);
        }
    }


    // accessToken 정보확인
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) { //access토큰이 만료됐을때 처리
            return e.getClaims();
        }
    }

    public UUID getUserId(String token) {
        String userIdString = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        return UUID.fromString(userIdString);
    }

}