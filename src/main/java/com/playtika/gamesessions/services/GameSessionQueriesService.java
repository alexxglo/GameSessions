package com.playtika.gamesessions.services;

import com.playtika.gamesessions.models.FilterInterface;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GameSessionQueriesService implements FilterInterface {

    @Autowired
    GameSessionRepository gameSessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterService filterService;

    @Autowired
    private EntityManagerService entityManagerService;

    public Page<GameSession> getAll(Pageable pageable) {
        return gameSessionRepository.findAll(pageable);
    }

    public GameSession getById(long id) {
        return gameSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid id"));
    }

    public List<GameSession> getSessionsSortedAlphabeticallyByGameName(Pageable pageable) {
        return gameSessionRepository.findAll(pageable)
                .stream()
                .sorted(Comparator.comparing(GameSession::getName))
                .collect(Collectors.toList());
    }

    public List<GameSession> getSessionsWithDurationHigherThanValue(int value, Pageable pageable) {
        return gameSessionRepository.findAll(pageable)
                .stream()
                .filter(session -> session.getDuration() > value)
                .collect(Collectors.toList());
    }

    public List<GameSession> getSessionsSortedByStartDate(Pageable pageable) {
        return gameSessionRepository.findAll(pageable)
                .stream()
                .sorted(Comparator.comparing(GameSession::getStartDate))
                .collect(Collectors.toList());
    }

    public GameSession getEarliestSession() {
        return gameSessionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GameSession::getEndDate))
                .findFirst().orElseThrow(() -> new ArrayIndexOutOfBoundsException("No sessions found"));
    }


    @Override
    public List<GameSession> getListFromFilterQuery(String query) {
        String sqlQuery;
        sqlQuery = "SELECT * FROM game_sessions WHERE " + query;
        return entityManagerService.executeSqlCommand(sqlQuery);
    }
}
