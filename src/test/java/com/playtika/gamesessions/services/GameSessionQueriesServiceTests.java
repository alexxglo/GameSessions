package com.playtika.gamesessions.services;

import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.Role;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.RoleRepository;
import com.playtika.gamesessions.security.repositories.UserRepository;
import com.playtika.gamesessions.security.services.UserQueryService;
import com.playtika.gamesessions.security.services.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class GameSessionQueriesServiceTests {

    @MockBean
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionQueriesService gameSessionQueriesService;

    private User userMockNoData = mock(User.class);

    @Mock
    private Pageable pageable;

    @MockBean
    private EntityManagerService entityManagerService;

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
    public void goodFlowGetAllTest() {
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Page<GameSession> pageGameSessionList = new PageImpl<>(gameSessions);

        when(gameSessionRepository.findAll(pageable)).thenReturn(pageGameSessionList);
        assertThat(gameSessionQueriesService.getAll(pageable)).isEqualTo(pageGameSessionList);
    }

    @Test
    public void noDataGetAllTest() {
        when(gameSessionRepository.findAll(pageable)).thenThrow(new RuntimeException());
        assertThatThrownBy(() -> gameSessionQueriesService.getAll(pageable)).isInstanceOf(Exception.class);
    }

    @Test
    public void existentIdGetById() {
        GameSession gameSession = configureGameSessionMock();

        when(gameSessionRepository.findById(gameSession.getUser().getId())).thenReturn(Optional.of(gameSession));
        assertThat(gameSessionQueriesService.getById(gameSession.getUser().getId()).getName())
                .isEqualTo(gameSession.getName());
    }

    @Test
    public void getSessionsSortedAlphabeticallyByGameNameTest() {
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Page<GameSession> pageGameSessionList = new PageImpl<>(gameSessions);

        when(gameSessionRepository.findAll(pageable)).thenReturn(pageGameSessionList);

        assertThat(gameSessionQueriesService.getSessionsSortedAlphabeticallyByGameName(pageable)).isEqualTo(gameSessions);
    }

    @Test
    public void getSessionsWithDurationHigherThanValueTest() {
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Page<GameSession> pageGameSessionList = new PageImpl<>(gameSessions);

        when(gameSessionRepository.findAll(pageable)).thenReturn(pageGameSessionList);
        when(gameSession.getDuration()).thenReturn(10);

        assertThat(gameSessionQueriesService.getSessionsWithDurationHigherThanValue(5, pageable)).isEqualTo(gameSessions);

    }

    @Test
    public void getSessionsSortedByStartDate() {
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Page<GameSession> pageGameSessionList = new PageImpl<>(gameSessions);
        Date date = new Date();

        when(gameSessionRepository.findAll(pageable)).thenReturn(pageGameSessionList);
        when(gameSession.getStartDate()).thenReturn(date);

        assertThat(gameSessionQueriesService.getSessionsSortedByStartDate(pageable)).isEqualTo(gameSessions);

    }

    @Test
    public void getEarliestSessionTest() {
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        Date date = new Date();

        when(gameSessionRepository.findAll()).thenReturn(gameSessions);
        when(gameSession.getEndDate()).thenReturn(date);

        assertThat(gameSessionQueriesService.getEarliestSession()).isEqualTo(gameSession);

    }

    @Test
    public void getListFromFilterQueryTest() {
        String query = "test";
        GameSession gameSession = configureGameSessionMock();
        List<GameSession> gameSessions = new ArrayList<>();
        gameSessions.add(gameSession);
        String returnQuery = "SELECT * FROM game_sessions WHERE " + query;

        when(entityManagerService.executeSqlCommand(returnQuery))
                .thenReturn(gameSessions);

        assertThat(gameSessionQueriesService.getListFromFilterQuery(query)).isEqualTo(gameSessions);
    }
}
