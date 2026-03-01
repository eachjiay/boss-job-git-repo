package com.bossjob.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bossjob.dto.LoginDTO;
import com.bossjob.dto.RegisterDTO;
import com.bossjob.entity.User;
import com.bossjob.mapper.UserMapper;
import com.bossjob.security.JwtTokenProvider;
import com.bossjob.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User register(RegisterDTO registerDTO) {
        if (findByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        return user;
    }

    @Override
    public String login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    public String loginByPhone(String phone) {
        User user = findByPhone(phone);
        // 如果用户不存在，则自动注册
        if (user == null) {
            user = new User();
            user.setUsername("user_" + phone);
            // 生成随机密码并加密
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setPhone(phone);
            user.setRole("SEEKER"); // 默认角色为求职者
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            save(user);
        }

        // Build a UserDetails principal so JwtTokenProvider can cast it
        org.springframework.security.core.userdetails.UserDetails userDetails = buildUserDetails(user);
        return authenticateAndGenerateToken(userDetails);
    }

    @Override
    public String loginByEmail(String email) {
        User user = findByEmail(email);
        // 如果邮箱不存在，自动注册
        if (user == null) {
            user = new User();
            user.setUsername(email.split("@")[0]); // 使用邮箱前缀作为用户名
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setEmail(email);
            user.setRole("SEEKER");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            save(user);
        }

        org.springframework.security.core.userdetails.UserDetails userDetails = buildUserDetails(user);
        return authenticateAndGenerateToken(userDetails);
    }

    @Override
    public String loginByWechat(String openId) {
        User user = findByWechatOpenId(openId);
        // 如果未绑定过微信，则自动注册一个随机用户
        if (user == null) {
            user = new User();
            String prefix = "wx_";
            user.setUsername(prefix + openId.substring(Math.max(0, openId.length() - 8)));
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setWechatOpenId(openId);
            user.setRole("SEEKER");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            save(user);
        }

        org.springframework.security.core.userdetails.UserDetails userDetails = buildUserDetails(user);
        return authenticateAndGenerateToken(userDetails);
    }

    private org.springframework.security.core.userdetails.UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword() != null ? user.getPassword() : "",
                user.getStatus() == 1, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    private String authenticateAndGenerateToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    public User findByUsername(String username) {
        return getOne(new QueryWrapper<User>()
                .eq("username", username)
                .or().eq("phone", username)
                .or().eq("email", username));
    }

    @Override
    public User findByPhone(String phone) {
        return getOne(new QueryWrapper<User>().eq("phone", phone));
    }

    @Override
    public User findByEmail(String email) {
        return getOne(new QueryWrapper<User>().eq("email", email));
    }

    @Override
    public User findByWechatOpenId(String openId) {
        return getOne(new QueryWrapper<User>().eq("wechat_open_id", openId));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return findByUsername(username);
    }

    @Override
    public User getByUsername(String username) {
        return findByUsername(username);
    }
}