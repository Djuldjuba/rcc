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
import io.student.rococo.service.UsersClient;
import org.jspecify.annotations.NonNull;
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

    @Override
    @Step("Создать пользователя с username '{username}'")
    public @NonNull UserJson createUser(@NonNull String username, @NonNull String password) {
        return createUser(username, password, true);
    }

    @Step("Создать пользователя с username '{username}'")
    public @NonNull UserJson createUser(@NonNull String username, @NonNull String password, boolean enabled) {
        UUID userId = UUID.randomUUID();
        String encodedPassword = passwordEncoder.encode(password);

        AuthUserEntity authUser = createAuthUserEntity(userId, username, encodedPassword, enabled);
        authUserRepository.create(authUser);

        UserEntity apiUser = createApiUserEntity(userId, username);
        userRepository.create(apiUser);

        return new UserJson(userId, username, null, null, null);
    }

    public Optional<UserJson> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserJson::fromEntity);
    }

    @Step("Удалить пользователя с username '{username}'")
    public void deleteUserByUsername(@NonNull String username) {
        // Удаляем из Auth базы
        Optional<AuthUserEntity> authUser = authUserRepository.findByUsername(username);
        if (authUser.isPresent()) {
            AuthUserEntity user = authUser.get();
            if (user.getAuthorities() != null) {
                user.getAuthorities().forEach(authUserRepository::deleteAuthority);
            }
            authUserRepository.delete(user);
        }

        // Удаляем из API базы
        Optional<UserEntity> apiUser = userRepository.findByUsername(username);
        apiUser.ifPresent(userRepository::delete);
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