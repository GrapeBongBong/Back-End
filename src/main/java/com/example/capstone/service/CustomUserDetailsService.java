package com.example.capstone.service;

import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {

        return (UserDetails) userRepository.findById(username)
                .map(user -> createUser(username, user))
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
        //return (UserDetails) userRepository.findOneWithRolesById(String.valueOf(username)).orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));

    }

   private org.springframework.security.core.userdetails.User createUser(String id, UserEntity user) {
       /* if (!user.isActivated()) {
            throw new RuntimeException(id + " -> 활성화되어 있지 않습니다.");
        }*/

        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getRoleName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getId(),
                user.getPassword(),
                grantedAuthorities);



    }

}