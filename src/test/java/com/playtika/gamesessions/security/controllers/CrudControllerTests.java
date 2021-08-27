package com.playtika.gamesessions.security.controllers;
import com.playtika.gamesessions.GameSessionsTrackerApplication;
import com.playtika.gamesessions.security.dto.LoginRequest;
import com.playtika.gamesessions.security.dto.LoginResponse;
import com.playtika.gamesessions.security.services.UserService;
import jdk.jfr.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CrudControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LoginResponse loginResponse;

    @MockBean
    private LoginRequest loginRequest;

    @MockBean
    private Authentication auth;

    @Test
    public void noInputLoginTest() throws Exception {
        mockMvc.perform(get("/login")).andExpect(MockMvcResultMatchers.content().string("Login page"));
    }

    @Test
    public void okLoginTest() throws Exception {
        LoginResponse mockedResponse = new LoginResponse();
        mockedResponse.setUserName("test");
        mockedResponse.setEmail("test@test");
        mockedResponse.setAccessToken("1231245125");
        when(userService.login(anyString(),anyString())).thenReturn(null);
        mockMvc.perform(post("/login")).andExpect(status().isFound());
//                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value("test"));
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
}
