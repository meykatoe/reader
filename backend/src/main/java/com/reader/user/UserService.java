package com.reader.user;

import com.reader.common.ApiException;
import com.reader.common.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(ErrorCode.USERNAME_TAKEN);
        }
        userRepository.save(new User(username, passwordEncoder.encode(password)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(AuthUser::from)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
