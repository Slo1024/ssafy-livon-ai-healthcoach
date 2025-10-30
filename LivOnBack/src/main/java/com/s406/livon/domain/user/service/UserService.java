package com.s406.livon.domain.user.service;


import com.s406.livon.domain.user.dto.JwtToken;
import com.s406.livon.domain.user.dto.request.*;
import com.s406.livon.domain.user.dto.response.HealthSurveyResponseDto;
import com.s406.livon.domain.user.dto.response.MyInfoResponseDto;
import com.s406.livon.domain.user.dto.response.OrganizationsResponseDto;
import com.s406.livon.domain.user.dto.response.UserDto;
import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.CoachInfoRepository;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import com.s406.livon.domain.user.repository.OrganizationsRepository;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.handler.CoachHandler;
import com.s406.livon.global.error.handler.TokenHandler;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final HealthSurveyRepository healthSurveyRepository;
    private final OrganizationsRepository organizationsRepository;
    private final CoachInfoRepository coachInfoRepository;




    @Transactional
    public JwtToken signIn(String username, String password) {

        // 1. email + password 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        try {

            // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Master 에 대한 검증 진행
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. 인증 정보를 기반으로 JWT 토큰 생성
//            JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
            JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

            // Refresh Token을 Redis에 저장
            redisTemplate.opsForValue()
                    .set("RT:" + authentication.getName(), jwtToken.getRefreshToken(), jwtToken.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

            log.info("[signIn] 로그인 성공: email = {}", username);
            return jwtToken;
        } catch (BadCredentialsException e) {
            log.error("[signIn] 로그인 실패: 잘못된 아이디 및 비밀번호, email = {}", username);
            throw new UserHandler(ErrorStatus.USER_INVALID_CREDENTIALS);  // 'INVALID_CREDENTIALS' 에러 코드로 구체적인 비밀번호 오류 처리
        } catch (Exception e) {
            log.error("[signIn] 로그인 실패: email = {}, 오류 = {}", username, e.getMessage());
            throw new UserHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }



    @Transactional
    public UserDto signUp(SignUpDto signUpDto) {
        log.info("[signUp] 회원가입 요청: email = {}", signUpDto.getEmail());

        // 중복 검증
        checkEmailDuplicate(signUpDto.getEmail());
        checknicknameDuplicate(signUpDto.getNickname());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());

        // 조직 값이 null인지 아닌지 확인하고 분기처리
        Organizations organizations = null;
        if (signUpDto.getOrganizations() != null) {
            organizations = organizationsRepository.findByName(signUpDto.getOrganizations())
                    .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND_ORGANIZATIONS));
            log.info("[signUp] 기업 회원 가입: organization = {}", signUpDto.getOrganizations());
        } else {
            log.info("[signUp] 개인 회원 가입");
        }

        User savedUser = userRepository.save(signUpDto.toEntity(encodedPassword, organizations));
        return UserDto.toDto(savedUser);
    }



    @Transactional
    public JwtToken reissue(ReissueDto reissueDto) {
        log.info("[reissue] 토큰 갱신 요청: accessToken = {}", reissueDto.getAccessToken());


        // RefreshToken 검증
        if (!jwtTokenProvider.validateToken(reissueDto.getRefreshToken())) {
            log.warn("[reissue] RefreshToken 유효하지 않음: refreshToken = {}", reissueDto.getRefreshToken());
            throw new TokenHandler(ErrorStatus.REFRESH_TOKEN_NOT_VALID);
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(reissueDto.getAccessToken());
        String refreshToken = (String) redisTemplate.opsForValue().get("RT:" + authentication.getName());

        if (refreshToken == null) {
            throw new TokenHandler(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        if (!refreshToken.equals(reissueDto.getRefreshToken())) {
            log.warn("[reissue] RefreshToken 불일치: email = {}", authentication.getName());
            throw new TokenHandler(ErrorStatus.REFRESH_TOKEN_NOT_MATCH);
        }

        // 새 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // RefreshToken Redis 업데이트
        redisTemplate.opsForValue()
                .set("RT:" + authentication.getName(), jwtToken.getRefreshToken(), jwtToken.getRefreshTokenExpirationTime(), TimeUnit.MILLISECONDS);

        log.info("[reissue] 토큰 갱신 성공: email = {}", authentication.getName());
        return jwtToken;
    }

    @Transactional(readOnly = true)
    public MyInfoResponseDto myInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // User와 연관된 HealthSurvey 조회 (없을 수 있음)
        HealthSurvey healthSurvey = user.getHealthSurvey();

        // HealthSurveyResponseDto 생성 (없으면 기본값)
        HealthSurveyResponseDto healthSurveyDto = HealthSurveyResponseDto.toDTO(healthSurvey);

        return MyInfoResponseDto.builder()
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .gender(user.getGender())
                .birthdate(user.getBirthdate())
                .healthSurvey(healthSurveyDto)
                .build();
    }

    @Transactional
    public String checkEmailDuplicate(String email) {
        // 이메일 중복체크
        if (userRepository.existsByEmail(email)) {
            throw new UserHandler(ErrorStatus.USER_ID_IN_USE);
        }
        return "사용가능한 이메일입니다";

    }

    @Transactional
    public String checknicknameDuplicate(String nickname) {
        // 닉네임 중복체크
        if (userRepository.existsByNickname(nickname)) {
            throw new UserHandler(ErrorStatus.USER_NICKNAME_IN_USE);
        }
        return "사용가능한 닉네임입니다";
    }

    @Transactional
    public String resetPassword(ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findByEmail(resetPasswordDto.getEmail())
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        String uuid = (String) redisTemplate.opsForValue().get("UUID:" + resetPasswordDto.getUuid());
        if (uuid == null || !uuid.equals(resetPasswordDto.getEmail())) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        String encodedPassword = passwordEncoder.encode(resetPasswordDto.getPassword());
        user.setPassword(encodedPassword);

        redisTemplate.delete("UUID:" + resetPasswordDto.getUuid());
        return "비밀번호가 변경되었습니다.";
    }

    @Transactional
    public void mypageResetPassword(UUID userId, String exsPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        String encodedExistingPassword = user.getPassword();
        String encodedExsPassword = passwordEncoder.encode(exsPassword);
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(exsPassword, encodedExistingPassword)) {
            throw new UserHandler(ErrorStatus.PASSWORD_NOT_MATCH);
        }

        // 새 비밀번호가 기존과 동일한지 확인
        if (passwordEncoder.matches(newPassword, encodedExistingPassword) ||
                passwordEncoder.matches(newPassword, encodedExsPassword)) {
            throw new UserHandler(ErrorStatus.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(encodedNewPassword);
    }

    /**
     * 닉네임 변경
     *
     * @param userId
     * @param nickname
     * @return
     */

    @Transactional
    public String mypageResetNickname(UUID userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        if (userRepository.existsByNickname(nickname)) {
            throw new UserHandler(ErrorStatus.USER_NICKNAME_IN_USE);
        }
        user.setNickname(nickname);

        return "닉네임이 변경되었습니다";
    }

    /**
     * 프로필 이미지 변경
     * @param userId 사용자 식별자
     * @param profileImageUrl 변경할 프로필 이미지 URL
     */
    @Transactional
    public void updateProfileImage(UUID userId, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        user.setProfileImage(profileImageUrl);
    }


    @Transactional
    public String logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        if (redisTemplate.opsForValue().get("RT:" + user.getId()) != null) {
            redisTemplate.delete("RT:" + user.getId());
        }
//        fcmTokenRepository.deleteAllByUser(user);

        return "로그아웃 되었습니다.";
    }


    /**
     * 회원 탈퇴 처리
     *
     * <p>사용자가 탈퇴할 때 개인 루틴, 단체 루틴 참여 정보, 관련 기록 및 FCM 토큰 등을
     * 모두 삭제한 뒤 최종적으로 사용자 정보를 제거합니다.</p>
     *
     * @param userId 탈퇴할 사용자 ID
     */
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // Refresh Token 제거
        if (redisTemplate.opsForValue().get("RT:" + user.getId()) != null) {
            redisTemplate.delete("RT:" + user.getId());
        }
        // FCM 토큰 제거
//        fcmTokenRepository.deleteAllByUser(user);

        // 최종적으로 사용자 삭제
        userRepository.delete(user);
    }

    @Transactional
    public String healthSurvey(UUID userId, HealthSurveyRequestDto healthSurveyRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        HealthSurvey healthSurvey = healthSurveyRequestDto.toEntity(user);
        if(healthSurveyRepository.existsById(user.getId())){
            HealthSurvey  healthSurveyUpdate= healthSurveyRepository.findById(user.getId())
                    .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND_HEALTH));
            healthSurveyUpdate.update(healthSurveyRequestDto);
            return "생체 데이터가 업데이트 되었습니다.";

        }else{
            healthSurveyRepository.save(healthSurvey);
        }

        return "생체 데이터가 저장되었습니다.";
    }

    public List<OrganizationsResponseDto> allOrganizations() {
        return organizationsRepository.findAll().stream().map(OrganizationsResponseDto::toDto).toList();

    }

    @Transactional
    public String coachInfo(UUID userId, CoachInfoRequestDto coachInfoRequestDto) {
        User coach = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 코치가 아닌 경우 예외 처리
        if (!coach.isCoach()) {
            throw new CoachHandler(ErrorStatus.USER_NOT_COACH);
        }

        CoachInfo coachInfo = coachInfoRequestDto.toEntity(coach);
        coachInfoRepository.save(coachInfo);
        return "코치 정보가 저장되었습니다.";
    }
}
