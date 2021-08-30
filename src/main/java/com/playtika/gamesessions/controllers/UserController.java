package com.playtika.gamesessions.controllers;

import com.playtika.gamesessions.security.dto.*;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.services.JwtTokenService;
import com.playtika.gamesessions.security.services.UserQueryService;
import com.playtika.gamesessions.security.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    UserQueryService queryUserService;

    @GetMapping
    @RequestMapping("/login")
    public String login(){
        return "Login page";
    }

    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws RuntimeException {

        LoginResponse loginResponse = userService.login(request.getUserName(), request.getPassword());
        if(loginResponse == null){
            throw new RuntimeException("Login failed. Possible cause : incorrect username/password");
        }else{
            return new ResponseEntity<>(loginResponse, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<User> signUp(@RequestBody SignUpRequest request) throws RuntimeException {

        User user;
        try {
            user = userService.signUp(request);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            throw e;
        }
    }

    @DeleteMapping(value = "/delete", params = {"userName"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<String> deleteUser(@RequestParam String userName) throws RuntimeException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String requsterUsername = auth.getName();
                userService.removeUser(userName, requsterUsername);
        } catch (Exception e) {
            throw e;
        }
        return new ResponseEntity<>(userName, HttpStatus.OK);
    }

    @GetMapping(value = "/users")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<List<User>> getAllUser(Pageable pageable) throws RuntimeException {
        try {
            return new ResponseEntity<>(queryUserService.getAllUser(pageable), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping(value = "/search", params = {"username"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<UserDTO> searchUser(@RequestParam String username) throws RuntimeException {
        UserDTO userResponse = userService.searchUser(username);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/refresh")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public String refreshToken(HttpServletRequest req) {
        return userService.refreshToken(req.getRemoteUser());
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<User> updateUserById(@RequestBody PatchUser patchUser, @PathVariable long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.updateUserById(patchUser, id, username);
        if(user == null) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }
    @PostMapping("/update")
    public ResponseEntity<User> updateUserSelf(@RequestBody PatchUser patchUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth.isAuthenticated()) {
            String username = auth.getName();
            User user = userService.updateUserSelf(patchUser, username);
                return new ResponseEntity<>(user, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(value = "/settings", params = {"maxPlaytime"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER') or hasRole('ROLE_USER')")
    public User updatePlaytime(@RequestParam int maxPlaytime) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.updatePlaytime(maxPlaytime, auth.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public User getUserById(@PathVariable long id) {
        return queryUserService.getUserById(id);
    }

    @GetMapping(params = {"min"})
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public List<User> getUsersAboveThresholdDailyTime(@RequestParam int min, Pageable pageable) {
        return queryUserService.getUsersAboveThresholdDailyTime(min, pageable);
    }

    @GetMapping("/highest/time")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public User getHighestDailyTimeUser() {
        return queryUserService.getHighestDailyTimeUser();
    }

    @GetMapping("/lowest/time")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    public User getLowestDailyTimeUser() {
        return queryUserService.getLowestDailyTimeUser();
    }

}
