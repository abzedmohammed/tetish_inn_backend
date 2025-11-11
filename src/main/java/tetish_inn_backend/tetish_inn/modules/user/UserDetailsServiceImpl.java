package tetish_inn_backend.tetish_inn.modules.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = null;
        try {
            user = userRepository.findByUsrEmail(username)
                    .orElseThrow(() -> new Exception("User not found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new UserDetailsImpl(user);
    }
}
