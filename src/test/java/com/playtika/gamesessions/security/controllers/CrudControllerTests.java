package com.playtika.gamesessions.security.controllers;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtika.gamesessions.exceptions.MyCustomException;
import com.playtika.gamesessions.security.dto.LoginRequest;
import com.playtika.gamesessions.security.dto.LoginResponse;
import com.playtika.gamesessions.security.dto.SignUpRequest;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.services.JwtTokenService;
import com.playtika.gamesessions.security.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private Authentication auth;

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

    @Test
    public void illegalCredentialsForSignUp() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUserName("hello");
        signUpRequest.setPassword("1234");
        signUpRequest.setEmail("12341@1241");
        MyCustomException e = new MyCustomException("whatever", HttpStatus.UNAUTHORIZED);
        when(userService.signUp(signUpRequest))
                .thenThrow(e);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                        .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));
    }
}
