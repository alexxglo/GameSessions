package com.playtika.gamesessions.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.security.dto.PatchUser;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.services.JwtTokenService;
import com.playtika.gamesessions.security.services.UserQueryService;
import com.playtika.gamesessions.security.services.UserService;
import com.playtika.gamesessions.services.GameSessionService;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class GameSessionControllerTests {

    @Autowired
    private MockMvc mockMvc;


    private Authentication auth = mock(Authentication.class);

    @Mock
    private User mockedUser;

    @MockBean
    private GameSessionService gameSessionService;

    @Mock
    private GameSession mockGameSession;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void startSessionTest() throws Exception {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("auth");
        when(mockGameSession.getName()).thenReturn("game");
        when(mockedUser.getUsername()).thenReturn("user");
        when(gameSessionService.startGame(mockGameSession.getName(), auth.getName())).thenReturn(mockGameSession);

        mockMvc.perform(post("/api/session/new")
                        .param("gameName", mockGameSession.getName()))
                        .andExpect(status().isOk());
    }
}
