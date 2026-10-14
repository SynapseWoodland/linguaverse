package cn.linguaverse.user.service;

import cn.linguaverse.common.BizException;
import cn.linguaverse.security.JwtService;
import cn.linguaverse.security.LoginRateLimiter;
import cn.linguaverse.user.User;
import cn.linguaverse.user.dto.RegisterRequest;
import cn.linguaverse.user.dto.UserViews.AuthResponse;
import cn.linguaverse.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginRateLimiter rateLimiter;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder,
                       JwtService jwtService, LoginRateLimiter rateLimiter) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String username = req.getUsername().trim();
        Long exists = userMapper.selectCount(new QueryWrapper<User>().eq("username", username));
        if (exists != null && exists > 0) {
            throw BizException.conflict("用户名已存在");
        }

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        String nickname = (req.getNickname() == null || req.getNickname().isBlank())
                ? username : req.getNickname().trim();
        u.setNickname(nickname);
        u.setAvatarSeed(Character.toString(Character.toUpperCase(username.charAt(0))));
        u.setRole("USER");
        u.setDailyGoalXp(30);
        u.setStreak(0);
        u.setOnboarded(false);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);
        userMapper.insert(u);

        return new AuthResponse(tokenOf(u), voOf(u));
    }

    public AuthResponse login(String username, String rawPassword, String ip) {
        String key = username == null ? "" : username.trim();
        rateLimiter.checkAllowed(key, ip);

        User u = userMapper.selectOne(new QueryWrapper<User>().eq("username", key));
        if (u == null || !passwordEncoder.matches(rawPassword, u.getPasswordHash())) {
            rateLimiter.recordFailure(key, ip);
            throw BizException.unauthorized("用户名或密码错误");
        }
        rateLimiter.recordSuccess(key, ip);
        return new AuthResponse(tokenOf(u), voOf(u));
    }

    private String tokenOf(User u) {
        return jwtService.issue(u.getId(), u.getUsername(), u.getRole());
    }

    private cn.linguaverse.user.dto.UserViews.UserVO voOf(User u) {
        return cn.linguaverse.user.dto.UserViews.UserVO.of(u);
    }
}
