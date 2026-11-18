package io.student.rcc.userdata.controller;

import io.student.rcc.userdata.domain.UpdateUserRequest;
import io.student.rcc.userdata.domain.UserResponse;
import io.student.rcc.userdata.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse currentUser(@AuthenticationPrincipal Jwt principal) {
        return userService.createNewUserIfNotPresent(principal.getClaim("sub"));
    }

    @PostMapping
    public UserResponse updateUser(@Valid @RequestBody UpdateUserRequest request,
                                   @AuthenticationPrincipal Jwt principal) {
        String username = principal.getClaim("sub");
        return userService.updateUser(username, request);
    }
}