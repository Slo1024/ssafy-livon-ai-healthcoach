package com.s406.livon.domain.user.service;


import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;


  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    return userRepository.findByEmail(username)
        .map(this::createUserDetails)
        .orElseThrow(() -> new UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다."));
  }


  // 해당하는 User 의 데이터가 존재한다면 UserDetails 객체로 만들어서 return
  private UserDetails createUserDetails(User user) {
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getId().toString())
        .password(user.getPassword())
        .roles(user.getRoles().stream().map(Role::name).toArray(String[]::new))
        .build();
  }

}