package com.playtika.gamesessions.services;

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
public class GameSessionQueriesService {

    @Autowired
    GameSessionRepository gameSessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FilterService filterService;

    @PersistenceContext
    private EntityManager em;

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

    public List<GameSession> getListFromSqlQuery(String input) {
        List<String> expressionsFromInput;
        List<String> safeExpressions = new ArrayList<>();
        String regexedString = new String();
        expressionsFromInput = filterService.resolveQuery(input);

        String sqlQuery = new String();
        Pattern pattern = Pattern.compile("[^;?\']");
        for (String exp : expressionsFromInput) {
            Matcher matcher = pattern.matcher(exp);
            while (matcher.find()) {
                regexedString = regexedString + matcher.group();
            }
            safeExpressions.add(regexedString);
            regexedString = new String();
        }
        for (String exp : safeExpressions) {
            String translatedOperation = checkIfKeywordExists(exp);
            if (translatedOperation != null) {
                sqlQuery = sqlQuery.concat(translatedOperation);
            } else {
                sqlQuery = sqlQuery.concat(exp);
            }
        }
        sqlQuery = "SELECT * FROM game_sessions WHERE " + sqlQuery;
        System.out.println(sqlQuery);
        return em.createNativeQuery(sqlQuery).getResultList();
    }

    private String checkIfKeywordExists(String exp) {
        if(" eq".equals(exp)) {
            return "=";
        }
        else if(" ne".equals(exp)) {
            return "!=";
        }
        else if(" gt".equals(exp)) {
            return ">";
        }
        else if(" lt".equals(exp)) {
            return "<";
        }
        else return null;
    }
}
