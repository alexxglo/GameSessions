package com.playtika.gamesessions.services;

import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GameSessionService {

    @Autowired
    GameSessionRepository gameSessionRepository;

    @Autowired
    UserRepository userRepository;


    public List<GameSession> getAll() {
        return gameSessionRepository.findAll();
    }

    public GameSession startGame(SessionDTO sessionDTO, String username) {
        GameSession gameSession = new GameSession();
        User currentUser = userRepository.findByUsername(username);

        //TODO verify integrity of game name

        if(!gameSessionRepository.getOngoingGameSessions(currentUser.getId()).isEmpty()) {
            return null; /*** Another game is active ***/
        }

        gameSession.setName(sessionDTO.getName());
        Date startDate = new Date();
        gameSession.setStartDate(startDate);
        gameSession.setUser(currentUser);
        return gameSessionRepository.saveAndFlush(gameSession);
    }

    public GameSession endGame(String username) {

        User currentUser = userRepository.findByUsername(username);
        long userId = currentUser.getId();
        boolean gameSessionsEmpty = gameSessionRepository.getOngoingGameSessions(userId).isEmpty();
        boolean multipleGameSessions = gameSessionRepository.getOngoingGameSessions(userId).size() >= 2;

        if(gameSessionsEmpty || multipleGameSessions ) {
            return null;
        }
        else {
            GameSession gameSession = gameSessionRepository.getOngoingGameSessions(userId).get(0);
            Date endDate = new Date();
            gameSession.setEndDate(endDate);
            int duration = (int) getDateDiff(gameSession.getStartDate(), endDate, TimeUnit.MINUTES);
            gameSession.setDuration(duration);
            return gameSessionRepository.saveAndFlush(gameSession);
        }
    }

    public String currentDuration(String username) {
        User currentUser = userRepository.findByUsername(username);
        long userId = currentUser.getId();
        boolean gameSessionsEmpty = gameSessionRepository.getOngoingGameSessions(userId).isEmpty();
        boolean multipleGameSessions = gameSessionRepository.getOngoingGameSessions(userId).size() >= 2;

        if(gameSessionsEmpty || multipleGameSessions ) {
            return null;
        }
        else {
            GameSession gameSession = gameSessionRepository.getOngoingGameSessions(userId).get(0);
            Date currentEndDate = new Date();
            int currentSessionDuration = (int) getDateDiff(gameSession.getStartDate(), currentEndDate, TimeUnit.MINUTES);
            return String.format("Current session duration: %d minutes", currentSessionDuration);
        }

    }

    private long getDateDiff(Date startDate, Date endDate, TimeUnit timeUnit) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }
}
