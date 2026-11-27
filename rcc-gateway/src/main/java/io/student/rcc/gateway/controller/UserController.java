package io.student.rcc.gateway.controller;

import io.student.rcc.gateway.model.UserJson;
import io.student.rcc.gateway.service.UserClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping
    public UserJson currentUser(@AuthenticationPrincipal Jwt principal) {
        if (principal == null) {
            throw new ResponseStatusException(FORBIDDEN, "Требуется аутентификация");
        }
        return userClient.getCurrentUser();
    }

    @PatchMapping
    public UserJson updateUser(@RequestBody UserJson user,
                               @AuthenticationPrincipal Jwt principal) {
        if (principal == null) {
            throw new ResponseStatusException(FORBIDDEN, "Требуется аутентификация");
        }
        String username = principal.getClaim("sub");
        if (!Objects.equals(username, user.username())) {
            throw new ResponseStatusException(FORBIDDEN, "Поле username обновлять нельзя");
        }
        return userClient.updateUser(user);
    }
}