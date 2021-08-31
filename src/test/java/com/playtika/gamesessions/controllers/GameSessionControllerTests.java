package com.playtika.gamesessions.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.services.FilterService;
import com.playtika.gamesessions.services.GameSessionQueriesService;
import com.playtika.gamesessions.services.GameSessionService;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import static org.mockito.ArgumentMatchers.*;
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

    @Autowired
    private ObjectMapper objectMapper;

    private Authentication auth = mock(Authentication.class);

    @Mock
    private User mockedUser;

    @MockBean
    private GameSessionService gameSessionService;

    @MockBean
    private FilterService filterService;

    @Mock
    private GameSession mockGameSession;

    @Mock
    private Pageable pageable;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private GameSessionQueriesService queriesService;

    private User configureUserMock() {
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

    @Before
    private SecurityContext configureSecurityContext() {
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("auth");
        SecurityContextHolder.setContext(securityContext);
        return securityContext;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void startSessionTest() throws Exception {
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
        Page<GameSession> gameSessionPage = new PageImpl(gameSessions);
        when(queriesService.getAll(any())).thenReturn(gameSessionPage);

        mockMvc.perform(get("/api/session/", "pageable"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(gameSessions));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void endSessionTest() throws Exception {
        GameSession gameSession = new GameSession();
        gameSession.setName("testSession");

        when(gameSessionService.endGame(any())).thenReturn(gameSession);

        mockMvc.perform(get("/api/session/end"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(gameSession.getName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getCurrentSessionDurationTest() throws Exception {
        String responseString = "Some minutes";

        when(gameSessionService.currentDuration(any())).thenReturn(responseString);

        mockMvc.perform(get("/api/session/info"))
                .andExpect(status().isOk())
                .andExpect(result -> responseString.equals(result));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getTodayPlaytimeTest() throws Exception {
        String responseString = "Playtime test minutes";

        when(gameSessionService.getTodayPlaytime(any())).thenReturn(responseString);

        mockMvc.perform(get("/api/session/today"))
                .andExpect(status().isOk())
                .andExpect(result -> responseString.equals(result));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getPlaytimeByDayTest() throws Exception {
        String responseString = "Some minutes";

        when(gameSessionService.getTotalPlaytimeByDay(anyString(), eq(responseString))).thenReturn(responseString);

        mockMvc.perform(post("/api/session/history")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(responseString))
                .andExpect(status().isOk())
                .andExpect(result -> responseString.equals(result));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void startCustomSessionTest() throws Exception {
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setName("test");
        GameSession gameSession = new GameSession();
        gameSession.setName("test");

        when(gameSessionService.customStartGame(eq(sessionDTO), anyString())).thenReturn(gameSession);

        mockMvc.perform(post("/api/session/custom-start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getEarliestSessionTest() throws Exception {
        GameSession gameSession = new GameSession();
        gameSession.setName("early session");

        when(queriesService.getEarliestSession()).thenReturn(gameSession);

        mockMvc.perform(get("/api/session/earliest"))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(gameSession));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getListFromSqlQuery() throws Exception {
        String query = "SQL query";
        GameSession gameSession = new GameSession();
        gameSession.setName("session");
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);

        when(filterService.getListFromFilterQuery(query)).thenReturn(gameSessions);

        mockMvc.perform(get("/api/session/custom")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(result -> result.equals(gameSession));
    }
}
