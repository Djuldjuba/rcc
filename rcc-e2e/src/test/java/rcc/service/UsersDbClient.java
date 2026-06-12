package rcc.service;

import rcc.config.Config;
import rcc.data.entity.AuthUserEntity;
import rcc.data.entity.Authority;
import rcc.data.entity.AuthorityEntity;
import rcc.data.entity.UserEntity;
import rcc.model.UserJson;
import rcc.repository.AuthUserRepository;
import rcc.repository.UserRepository;
import rcc.repository.tpl.XaTransactionTemplate;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class UsersDbClient implements UsersClient {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final AuthUserRepository authUserRepository = AuthUserRepository.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.rococoAuthUrl(),
            CFG.rococoApiUrl()
    );

    @Override
    public UserJson createUser(String username, String password) {
        return createUser(username, password, true);
    }

    public UserJson createUser(String username, String password, boolean enabled) {
        return xaTransactionTemplate.execute(() -> {
            UUID userId = UUID.randomUUID();
            String encodedPassword = passwordEncoder.encode(password);

            AuthUserEntity authUser = createAuthUserEntity(userId, username, encodedPassword, enabled);
            authUserRepository.create(authUser);

            UserEntity apiUser = createApiUserEntity(userId, username);
            userRepository.create(apiUser);

            return new UserJson(userId, username, null, null, null);
        });
    }

    private AuthUserEntity createAuthUserEntity(UUID userId, String username, String encodedPassword, boolean enabled) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setId(userId);
        authUser.setUsername(username);
        authUser.setPassword(encodedPassword);
        authUser.setEnabled(enabled);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);

        authUser.setAuthorities(
                Arrays.stream(Authority.values()).map(
                        authority -> {
                            AuthorityEntity ae = new AuthorityEntity();
                            ae.setId(UUID.randomUUID());
                            ae.setUser(authUser);
                            ae.setAuthority(authority);
                            return ae;
                        }).toList()
        );

        return authUser;
    }

    private UserEntity createApiUserEntity(UUID userId, String username) {
        UserEntity ue = new UserEntity();
        ue.setId(userId);
        ue.setUsername(username);
        ue.setFirstname(null);
        ue.setLastname(null);
        ue.setAvatar(null);
        return ue;
    }
}