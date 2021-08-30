package com.playtika.gamesessions.services;

import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.exceptions.OverAllocatedTimeException;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        int minutesAlreadyPlayedToday = gameSessionRepository.getDurationOnDay(currentUser.getId(), startDate);
        gameSession.setStartDate(startDate);
        gameSession.setUser(currentUser);
        if(minutesAlreadyPlayedToday > currentUser.getMaxDailyTime()) {
            gameSessionRepository.saveAndFlush(gameSession);
            throw new OverAllocatedTimeException();
        }
        return gameSessionRepository.saveAndFlush(gameSession);
    }


    public GameSession customStartGame(SessionDTO sessionDTO, String username) {
        GameSession gameSession = new GameSession();
        User currentUser = userRepository.findByUsername(username);

        if(!gameSessionRepository.getOngoingGameSessions(currentUser.getId()).isEmpty()) {
            throw new IllegalStateException(); /*** Another game is active ***/
        }

        gameSession.setName(sessionDTO.getName());
        gameSession.setStartDate(getYesterdayDate());
        gameSession.setUser(currentUser);
        return gameSessionRepository.saveAndFlush(gameSession);
    }

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
            if(isOnSameDay(gameSession.getStartDate(), endDate)) {
                return finishNewGameSession(gameSession, endDate);
            }
            else {
                setSecondGameSession(gameSession, endDate);
                Date firstSessionEndDate = setEndOfDay(gameSession.getStartDate());
                return finishNewGameSession(gameSession, firstSessionEndDate);
            }
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

    private boolean isOnSameDay(Date startDate, Date endDate) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(startDate);
        cal2.setTime(endDate);

        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

        return sameDay;
    }

    private GameSession finishNewGameSession(GameSession gameSession, Date endDate) {
        gameSession.setEndDate(endDate);
        int duration = (int) getDateDiff(gameSession.getStartDate(), endDate, TimeUnit.MINUTES);
        gameSession.setDuration(duration);
        return gameSessionRepository.saveAndFlush(gameSession);
    }

    private void setSecondGameSession(GameSession gameSession, Date endDate) {

        Date startOfDayOfSecondSession = resetDay(endDate);
        int duration = (int) getDateDiff(startOfDayOfSecondSession, endDate, TimeUnit.MINUTES);

        GameSession newGameSession = new GameSession();
        newGameSession.setUser(gameSession.getUser());
        newGameSession.setName(gameSession.getName());
        newGameSession.setStartDate(startOfDayOfSecondSession);
        newGameSession.setEndDate(endDate);
        newGameSession.setDuration(duration);
        gameSessionRepository.saveAndFlush(newGameSession);
    }

    private Date resetDay(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        return cal1.getTime();
    }

    private Date setEndOfDay(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        cal1.set(Calendar.HOUR_OF_DAY, 23);
        cal1.set(Calendar.MINUTE, 59);
        cal1.set(Calendar.SECOND, 59);
        return cal1.getTime();
    }
    private Date getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, (cal.get(Calendar.DAY_OF_MONTH) - 1));
        return cal.getTime();
    }

}
