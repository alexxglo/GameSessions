package com.playtika.gamesessions.security.controllers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtika.gamesessions.security.dto.*;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.services.JwtTokenService;
import com.playtika.gamesessions.security.services.UserQueryService;
import com.playtika.gamesessions.security.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CrudControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private UserService userService;

    private Authentication auth = mock(Authentication.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private UserQueryService queryUserService;

    @Mock
    private Pageable pageable;

    @Mock
    private User mockedUser;

    @Test
    public void noInputLoginTest() throws Exception {
        mockMvc.perform(get("/login")).andExpect(MockMvcResultMatchers.content().string("Login page"));
    }

    @Test
    public void okLoginTest() throws Exception {

        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUserName("test");
        loginResponse.setEmail("email");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserName("test");
        loginRequest.setPassword("email");
        when(userService.login(anyString(), anyString())).thenReturn(loginResponse);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value(loginResponse.getUserName()));
    }

    @Test
    public void usernameInvalidOnLoginTest() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));
    }

    @Test
    public void unauthorizedDeleteUserTest() throws Exception {
        mockMvc.perform(delete("/delete")
                        .param("userName", "unauthorizedUser"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void notLoggedInUpdateUserSelfTest() throws Exception {
        when(auth.isAuthenticated()).thenReturn(true);

        when(userService.updateUserSelf(any(), any())).thenReturn(null);
        mockMvc.perform(put("/update")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void unauthorizedUserGetRefreshTokenTest() throws Exception {
        mockMvc.perform(get("/refresh"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void okCredentialsForSignUp() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        when(userService.signUp(signUpRequest)).thenReturn(null);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void okDeleteTest() throws Exception {
        SecurityContextHolder securityContextHolder = mock(SecurityContextHolder.class);
        when(auth.getName()).thenReturn("mockedUser");
        mockMvc.perform(delete("/delete").param("userName", "userName"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("userName"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void searchUserTest() throws Exception {
        UserDTO userResponse = new UserDTO();
        userResponse.setUserName("test");
        when(userService.searchUser(anyString())).thenReturn(userResponse);
        mockMvc.perform(get("/search").param("username", "test")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllUsers() throws Exception {
        when(queryUserService.getAllUser(any())).thenThrow(new RuntimeException());
        mockMvc.perform(get("/users", pageable)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void refreshTokenTest() throws Exception {
        String refreshToken = "refresh";
        when(userService.refreshToken(any())).thenReturn(refreshToken);
        mockMvc.perform(get("/refresh")).andExpect(result -> result.equals(refreshToken));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateUserByInexistentIdTest() throws Exception {
        PatchUser patchUser = new PatchUser();
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        patchUser.setUserName("testUsername");
        patchUser.setPassword("1234");
        patchUser.setEmail("test@test.com");
        User user = mock(User.class);
        long testId = 5;
        String username = "test";
        when(auth.getName()).thenReturn(username);
        when(userService.updateUserById(patchUser, testId, username)).thenReturn(user);
        mockMvc.perform(put("/update/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateOkUserSelfTest() throws Exception {
        when(auth.isAuthenticated()).thenReturn(true);
        PatchUser patchUser = new PatchUser();
        patchUser.setUserName("testUsername");
        patchUser.setPassword("1234");
        patchUser.setEmail("test@test.com");
        String username = "test";
        User user = new User();
        user.setUsername(patchUser.getUserName());

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.isAuthenticated()).thenReturn(true);
        when(userService.updateUserSelf(patchUser, username)).thenReturn(user);
        mockMvc.perform(put("/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchUser)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updatePlaytimeTest() throws Exception {
        int playtime = 1;
        User mockUser = mock(User.class);
        when(mockUser.getMaxDailyTime()).thenReturn(playtime);
        when(auth.getName()).thenReturn("name");
        when(userService.updatePlaytime(playtime, auth.getName())).thenReturn(mockUser);
        mockMvc.perform(get("/settings").param("maxPlaytime", "1"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(mockUser));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserByExistentIdTest() throws Exception {
        long id = 1;
        when(queryUserService.getUserById(id)).thenReturn(mockedUser);
        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(mockedUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserByInexistentIdTest() throws Exception {
        long id = 1;
        when(queryUserService.getUserById(id)).thenThrow(new IllegalArgumentException());
        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUsersAboveThresholdDailyTimeTest() throws Exception {
        int min = 3;
        List<User> users = new ArrayList<>();
        users.add(mockedUser);

        when(queryUserService.getUsersAboveThresholdDailyTime(min, pageable)).thenReturn(users);
        mockMvc.perform(get("/", pageable)
                        .param("min", "3"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(users));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getHighestDailyTimeUserTest() throws Exception {

        when(queryUserService.getHighestDailyTimeUser()).thenReturn(mockedUser);
        mockMvc.perform(get("/highest/time"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(mockedUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void noUserGetHighestDailyTimeUserTest() throws Exception {

        when(queryUserService.getHighestDailyTimeUser()).thenThrow(new ArrayIndexOutOfBoundsException());
        mockMvc.perform(get("/highest/time"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ArrayIndexOutOfBoundsException));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getLowestDailyTimeUserTest() throws Exception {

        when(queryUserService.getLowestDailyTimeUser()).thenReturn(mockedUser);
        mockMvc.perform(get("/lowest/time"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(mockedUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void noUserGetLowestDailyTimeUserTest() throws Exception {

        when(queryUserService.getLowestDailyTimeUser()).thenThrow(new ArrayIndexOutOfBoundsException());
        mockMvc.perform(get("/lowest/time"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ArrayIndexOutOfBoundsException));
    }
}
