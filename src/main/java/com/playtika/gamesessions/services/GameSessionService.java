package com.playtika.gamesessions.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.exceptions.OverAllocatedTimeException;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.repositories.GameSessionRepository;
import com.playtika.gamesessions.security.models.User;
import com.playtika.gamesessions.security.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLException;
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

    @Autowired
    private WebClientService webClientService;

    private static final String API_KEY = "026aa61a92074996b999e40a57b2ba6b";


    public GameSession startGame(String gameName, String username) throws SSLException, JsonProcessingException {
        GameSession gameSession = new GameSession();
        User currentUser = userRepository.findByUsername(username);
        Date startDate = new Date();
        int minutesAlreadyPlayedToday = 0;
        gameName = checkedGame(gameName);

        if (gameName == null) {
            throw new IllegalArgumentException();
        }
        if (!gameSessionRepository.getOngoingGameSessions(currentUser.getId()).isEmpty()) {
            throw new IllegalStateException();
        }
        if (gameSessionRepository.getDurationOnDay(currentUser.getId(), startDate).isPresent()) {
            minutesAlreadyPlayedToday = gameSessionRepository.getDurationOnDay(currentUser.getId(), startDate).get();
        }
        gameSession = initialSetup(gameSession, gameName, startDate, currentUser);
        if (minutesAlreadyPlayedToday > currentUser.getMaxDailyTime()) {
            gameSessionRepository.saveAndFlush(gameSession);
            throw new OverAllocatedTimeException();
        }
        return gameSessionRepository.saveAndFlush(gameSession);
    }


    public GameSession customStartGame(SessionDTO sessionDTO, String username) {
        GameSession gameSession = new GameSession();
        User currentUser = userRepository.findByUsername(username);

        if (!gameSessionRepository.getOngoingGameSessions(currentUser.getId()).isEmpty()) {
            throw new IllegalStateException();
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

        if (gameSessionsEmpty || multipleGameSessions) {
            throw new IllegalStateException();
        } else {
            GameSession gameSession = gameSessionRepository.getOngoingGameSessions(userId).get(0);
            Date endDate = new Date();
            if (isOnSameDay(gameSession.getStartDate(), endDate)) {
                return finishNewGameSession(gameSession, endDate);
            } else {
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

        if (gameSessionsEmpty || multipleGameSessions) {
            return null;
        } else {
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
        if (!checkAuthentication(username)) {
            throw new AuthenticationException();
        }

        User currentUser = userRepository.findByUsername(username);
        Date date = new Date();
        return String.format("Today you have played for %d minutes", gameSessionRepository.getDurationOnDay(currentUser.getId(), date));
    }

    private long getDateDiff(Date startDate, Date endDate, TimeUnit timeUnit) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
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

    private String formatGameName(String gameName) {
        String[] splitedGameName = gameName.split("\\s+");
        String gameNameReformatted = new String();
        for (String word : splitedGameName) {
            gameNameReformatted += "-";
            gameNameReformatted += word;
        }
        gameNameReformatted = gameNameReformatted.substring(1);
        return gameNameReformatted;
    }

    private String existsGame(String gameName) throws SSLException, JsonProcessingException {

        String url = "https://rawg.io/api/games/" + gameName + "?key=" + API_KEY;
        String gameDetails = webClientService.getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> Mono.empty())
                .bodyToMono(String.class)
                .block();
        ObjectNode node = new ObjectMapper().readValue(gameDetails, ObjectNode.class);
        if (node.has("slug")) {
            System.out.println(node.get("slug"));
            return node.get("slug").asText();
        }
        return null;
    }

    private String checkedGame(String gameName) throws SSLException, JsonProcessingException {

        String gameNameReformatted = formatGameName(gameName);
        gameNameReformatted = existsGame(gameNameReformatted);

        return gameNameReformatted;
    }

    private GameSession initialSetup(GameSession gameSession, String gameName, Date startDate, User currentUser) {
        gameSession.setName(gameName);
        gameSession.setStartDate(startDate);
        gameSession.setUser(currentUser);
        return gameSession;
    }
}
