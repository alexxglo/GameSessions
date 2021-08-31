package com.playtika.gamesessions.controllers;

import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.services.GameSessionQueriesService;
import com.playtika.gamesessions.services.GameSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Mock
    private Pageable pageable;

    @MockBean
    private GameSessionQueriesService queriesService;

    public User configureUserMock() {
        User mockedUser = mock(User.class);
        Role role;
        List<Role> roles = new ArrayList<>();
        role = new Role();
        role.setName("ROLE_ADMIN");
        roles.add(role);

        when(mockedUser.getUsername()).thenReturn("user");
        when(mockedUser.getPassword()).thenReturn("pwd");
        when(mockedUser.getId()).thenReturn(1L);
        when(mockedUser.getRoles()).thenReturn(roles);


        return mockedUser;
    }

    private GameSession configureGameSessionMock() {
        User user = configureUserMock();
        GameSession mockedGameSession = mock(GameSession.class);
        int duration = 70;
        long id = 5;

        when(mockedGameSession.getName()).thenReturn("session");
        when(mockedGameSession.getDuration()).thenReturn(duration);
        when(mockedGameSession.getUser()).thenReturn(user);
        when(mockedGameSession.getUser().getId()).thenReturn(id);


        return mockedGameSession;
    }

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
                        .andExpect(status().isOk())
                        .andExpect(result -> result.equals(mockGameSession));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllTest() throws Exception {
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);

        when(queriesService.getAll(pageable).toList()).thenReturn(gameSessions);

        mockMvc.perform(get("/api/session/","pageable"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(gameSessions));
    }


}
