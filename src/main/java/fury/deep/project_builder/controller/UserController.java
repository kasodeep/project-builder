package fury.deep.project_builder.controller;

import fury.deep.project_builder.dto.user.UserDto;
import fury.deep.project_builder.dto.user.UserLoginRequest;
import fury.deep.project_builder.dto.user.UserRegisterRequest;
import fury.deep.project_builder.entity.user.User;
import fury.deep.project_builder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserDto userDto = userService.registerUser(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        UserDto userDto = userService.loginUser(userLoginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @GetMapping("/all/{teamId}")
    public ResponseEntity<List<UserDto>> getAllUsers(@PathVariable String teamId){
        return ResponseEntity.ok(userService.getUsersByTeamId(teamId));
    }
}
