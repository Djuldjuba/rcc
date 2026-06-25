package io.student.rococo.service.impl;

import io.qameta.allure.Step;
import io.student.rococo.config.Config;
import io.student.rococo.data.entity.AuthUserEntity;
import io.student.rococo.data.entity.Authority;
import io.student.rococo.data.entity.AuthorityEntity;
import io.student.rococo.data.entity.UserEntity;
import io.student.rococo.model.UserJson;
import io.student.rococo.data.repository.AuthUserRepository;
import io.student.rococo.data.repository.UserRepository;
import io.student.rococo.data.mapper.tpl.XaTransactionTemplate;
import io.student.rococo.service.UsersClient;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
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
    @Step("Создать пользователя с username '{username}'")
    public @NonNull UserJson createUser(@NonNull String username, @NonNull String password) {
        return createUser(username, password, true);
    }

    @Step("Создать пользователя с username '{username}'")
    public @NonNull UserJson createUser(@NonNull String username, @NonNull String password, boolean enabled) {
        UserJson result = xaTransactionTemplate.execute(() -> {
            UUID userId = UUID.randomUUID();
            String encodedPassword = passwordEncoder.encode(password);

            AuthUserEntity authUser = createAuthUserEntity(userId, username, encodedPassword, enabled);
            authUserRepository.create(authUser);

            UserEntity apiUser = createApiUserEntity(userId, username);
            userRepository.create(apiUser);

            return new UserJson(userId, username, null, null, null);
        });

        if (result == null) {
            throw new IllegalStateException("Failed to create user");
        }
        return result;
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