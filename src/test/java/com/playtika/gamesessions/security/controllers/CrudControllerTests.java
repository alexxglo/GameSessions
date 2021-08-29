package com.playtika.gamesessions.security.controllers;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtika.gamesessions.exceptions.MyCustomException;
import com.playtika.gamesessions.security.dto.*;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.services.JwtTokenService;
import com.playtika.gamesessions.security.services.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
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
        when(userService.login(anyString(),anyString())).thenReturn(loginResponse);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value(loginResponse.getUserName()));
    }

    @Test
    public void usernameInvalidOnLoginTest() throws Exception {
        when(userService.login(anyString(),anyString())).thenReturn(null);
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

        when(userService.updateUserSelf(any(),any())).thenReturn(null);
        mockMvc.perform(post("/update")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles="USER")
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

//    @Test
//    public void illegalCredentialsForSignUp() throws Exception {
//        SignUpRequest signUpRequest = new SignUpRequest();
//        signUpRequest.setUserName("hello");
//        signUpRequest.setPassword("1234");
//        signUpRequest.setEmail("12341@1241");
//        MyCustomException e = new MyCustomException("whatever", HttpStatus.UNAUTHORIZED);
//        when(userService.signUp(signUpRequest))
//                .thenThrow(e);
//        mockMvc.perform(post("/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(signUpRequest)))
//                        .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));
//    }

    @Test
    @WithMockUser(roles="ADMIN")
    public void okDeleteTest() throws Exception {
        SecurityContextHolder securityContextHolder = mock(SecurityContextHolder.class);
        when(auth.getName()).thenReturn("mockedUser");
        mockMvc.perform(delete("/delete").param("userName", "userName"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("userName"));
    }

    @Test
    @WithMockUser(roles="MANAGER")
    public void searchUserTest() throws Exception {
        UserDTO userResponse = new UserDTO();
        userResponse.setUserName("test");
        when(userService.searchUser(anyString())).thenReturn(userResponse);
        mockMvc.perform(get("/search").param("username", "test")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    public void getAllUsers() throws Exception {
        when(userService.getAllUser()).thenThrow(new RuntimeException());
        mockMvc.perform(get("/users")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    public void refreshTokenTest() throws Exception {
        String refreshToken = "refresh";
        when(userService.refreshToken(any())).thenReturn(refreshToken);
        mockMvc.perform(get("/refresh")).andExpect(result -> result.equals(refreshToken));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    public void updateUserByInexistentIdTest() throws Exception {
        PatchUser patchUser = new PatchUser();
        SecurityContextHolder securityContextHolder = mock(SecurityContextHolder.class);
        patchUser.setUserName("testUsername");
        patchUser.setPassword("1234");
        patchUser.setEmail("test@test.com");
        User user = mock(User.class);
        long testId = 5;
        String username = "test";
        when(auth.getName()).thenReturn(username);
        when(userService.updateUserById(patchUser, testId, username)).thenReturn(user);
        mockMvc.perform(post("/update/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchUser)))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    @WithMockUser(roles="ADMIN")
//    public void updateUserByExistentIdTest() throws Exception {
//        PatchUser patchUser = new PatchUser();
//        SecurityContextHolder securityContextHolder = mock(SecurityContextHolder.class);
//        patchUser.setUserName("testUsername");
//        patchUser.setPassword("1234");
//        patchUser.setEmail("test@test.com");
//        User user = new User();
//        user.setUsername(patchUser.getUserName());
//        long testId = 5;
//        String username = "test";
//        when(auth.getName()).thenReturn(username);
//        when(userService.updateUserById(patchUser, testId, username)).thenReturn(user);
//        mockMvc.perform(post("/update/{id}", testId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(patchUser)))
//                .andExpect(status().isOk());
//    }

    @Test
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
        mockMvc.perform(post("/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchUser)))
                .andExpect(status().isOk());
    }
}
