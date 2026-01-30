package fury.deep.project_builder.controller;

import fury.deep.project_builder.dto.user.UserLoginRequest;
import fury.deep.project_builder.dto.user.UserLoginResponse;
import fury.deep.project_builder.dto.user.UserRegisterRequest;
import fury.deep.project_builder.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Validated @RequestBody UserRegisterRequest userRegisterRequest) {
        userService.registerUser(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
