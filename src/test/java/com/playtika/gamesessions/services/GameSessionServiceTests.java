package com.playtika.gamesessions.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playtika.gamesessions.exceptions.OverAllocatedTimeException;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLException;
import java.text.ParseException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class GameSessionServiceTests {

    @Autowired
    private GameSessionService gameSessionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GameSessionRepository gameSessionRepository;

    private GameSession gameSessioNoData = mock(GameSession.class);

    @Mock
    private Pageable pageable;

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

        when(mockedGameSession.getName()).thenReturn("session");
        when(mockedGameSession.getDuration()).thenReturn(70);
        when(mockedGameSession.getUser()).thenReturn(user);


        return mockedGameSession;
    }

    @Test
    public void goodFlowStartGameTest() throws SSLException, JsonProcessingException {
        User user = configureUserMock();
        GameSession gameSession = configureGameSessionMock();
        Date date = new Date();
        List<GameSession> gameSessions = new ArrayList<>();
        Optional<Integer> duration = Optional.of(5);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSessionRepository.getOngoingGameSessions(user.getId())).thenReturn(gameSessions);
        when(gameSessionRepository.getDurationOnDay(user.getId(), date)).thenReturn(duration);
        when(user.getMaxDailyTime()).thenReturn(200);
        when(gameSessionRepository.saveAndFlush(any())).thenReturn(gameSession);
        assertThat(gameSessionService.startGame(gameSession.getName(), user.getUsername())
                .getName())
                .isEqualTo(gameSession.getName());
    }

    @Test
    public void gameNullStartGameTest() {
        User user = configureUserMock();
        List<GameSession> gameSessions = new ArrayList<>();

        GameSession gameSession = configureGameSessionMock();
        gameSessions.add(gameSession);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSessionRepository.getOngoingGameSessions(user.getId())).thenReturn(gameSessions);

        assertThatThrownBy(() -> gameSessionService.startGame(gameSession.getName(), user.getUsername()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void overTimeStartGameTest() {
        User user = configureUserMock();
        List<GameSession> gameSessions = new ArrayList<>();
        Date date = new Date();
        Optional<Integer> duration = Optional.of(50);

        GameSession gameSession = configureGameSessionMock();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSessionRepository.getOngoingGameSessions(user.getId())).thenReturn(gameSessions);
        when(gameSessionRepository.getDurationOnDay(user.getId(), date)).thenReturn(duration);
        when(user.getMaxDailyTime()).thenReturn(-5);
        when(gameSessionRepository.saveAndFlush(any(GameSession.class))).thenReturn(gameSession);

        assertThatThrownBy(() -> gameSessionService.startGame(gameSession.getName(), user.getUsername()))
                .isInstanceOf(OverAllocatedTimeException.class);
    }

    @Test
    public void goodFlowEndGameTest() {
        User user = configureUserMock();
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Date date = new Date();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSessionRepository.getOngoingGameSessions(user.getId())).thenReturn(gameSessions);
        when(gameSession.getStartDate()).thenReturn(date);
        when(gameSessionRepository.saveAndFlush(any(GameSession.class))).thenReturn(gameSession);
        assertThat(gameSessionService.endGame(user.getUsername())
                .getName())
                .isEqualTo(gameSession.getName());
    }

    @Test
    public void goodFlowTwoSessionsEndGameTest() {
        User user = configureUserMock();
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) - 1));
        Date date = cal.getTime();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSessionRepository.getOngoingGameSessions(user.getId())).thenReturn(gameSessions);
        when(gameSession.getStartDate()).thenReturn(date);
        when(gameSessionRepository.saveAndFlush(any(GameSession.class))).thenReturn(gameSession);
        assertThat(gameSessionService.endGame(user.getUsername())
                .getName())
                .isEqualTo(gameSession.getName());
    }

    @Test
    public void goodFlowCurrentDurationTest() {
        User user = configureUserMock();
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Date date = new Date();
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSession.getStartDate()).thenReturn(date);
        when(gameSessionRepository.getOngoingGameSessions(user.getId())).thenReturn(gameSessions);
        when(gameSessionRepository.saveAndFlush(any(GameSession.class))).thenReturn(gameSession);

        assertThat(gameSessionService.currentDuration(user.getUsername())).isNotNull();
    }

    @Test
    public void usernameNullGetTodayPlaytime() throws ParseException {

        assertThatThrownBy(() -> gameSessionService.getTodayPlaytime(null))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void goodFlowGetTodayPlaytime() throws AuthenticationException {
        User user = configureUserMock();
        Date date = new Date();

        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(gameSessionRepository.getDurationOnDay(user.getId(), date)).thenReturn(Optional.of(5));

        assertThat(gameSessionService.getTodayPlaytime(user.getUsername()))
                .isNotNull();
    }
}
