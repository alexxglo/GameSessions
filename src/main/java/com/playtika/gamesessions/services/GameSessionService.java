package com.playtika.gamesessions.services;

import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
            throw new IllegalStateException(); /*** Another game is active ***/
        }

        gameSession.setName(sessionDTO.getName());
        Date startDate = new Date();
        gameSession.setStartDate(startDate);
        gameSession.setUser(currentUser);
        return gameSessionRepository.saveAndFlush(gameSession);
    }

    //TODO if session extends to 2 days, add 2 sessions

    public GameSession endGame(String username) {

        User currentUser = userRepository.findByUsername(username);
        long userId = currentUser.getId();
        boolean gameSessionsEmpty = gameSessionRepository.getOngoingGameSessions(userId).isEmpty();
        boolean multipleGameSessions = gameSessionRepository.getOngoingGameSessions(userId).size() >= 2;

        if(gameSessionsEmpty || multipleGameSessions ) {
            throw new IllegalStateException();
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

    public String getTotalPlaytimeByDay(String username, String dateToParse) throws ParseException {
        User currentUser = userRepository.findByUsername(username);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateConverted = dateToParse + " " + "00:00:00";
        Date date = df.parse(dateConverted);
        return String.format("You have played %d minutes on %s", gameSessionRepository.getDurationOnDay(currentUser.getId(), date), date);
    }

    //TODO check if duration > total duration allowed

    public String getTodayPlaytime(String username) throws AuthenticationException {
        if(!checkAuthentication(username)) {
            throw new AuthenticationException();
        }

        User currentUser = userRepository.findByUsername(username);
        Date date = new Date();
        return String.format("Today you have played for %d minutes", gameSessionRepository.getDurationOnDay(currentUser.getId(), date));
    }

    private long getDateDiff(Date startDate, Date endDate, TimeUnit timeUnit) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private boolean checkAuthentication(String username) {
        if (username == null || username.isEmpty())
            return false;
        return true;
    }
}
