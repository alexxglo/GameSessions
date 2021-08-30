package com.playtika.gamesessions.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.services.GameSessionQueriesService;
import com.playtika.gamesessions.services.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.net.ssl.SSLException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/session")
public class GameSessionController {

    @Autowired
    GameSessionService gameSessionService;

    @Autowired
    GameSessionQueriesService queriesService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<GameSession> getAll(Pageable pageable) {
        return queriesService.getAll(pageable).toList();
    }

    @PostMapping(value = "/new", params = {"gameName"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GameSession startSession(@RequestParam String gameName) throws SSLException, JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.startGame(gameName, auth.getName());
    }

    @GetMapping("/end")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GameSession endSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.endGame(auth.getName());
    }

    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public String getCurrentSessionDuration() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.currentDuration(auth.getName());
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public String getTodayPlaytime() throws AuthenticationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.getTodayPlaytime(auth.getName());
    }

    @PostMapping("/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public String getPlaytimeByDay(@RequestBody String date) throws ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.getTotalPlaytimeByDay(auth.getName(), date);
    }

    @PostMapping("/custom-start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GameSession startCustomSession(@RequestBody SessionDTO sessionDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.customStartGame(sessionDTO, auth.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public GameSession getGameSessionById(@PathVariable long id) {
        return queriesService.getById(id);
    }

    @GetMapping("/alphabetical")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GameSession> getSessionsSortedAlphabeticallyByGameName(Pageable pageable) {
        return queriesService.getSessionsSortedAlphabeticallyByGameName(pageable);
    }

    @GetMapping(params = {"durationHigherThan"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GameSession> getSessionsWithDurationHigherThanValue(int durationHigherThan, Pageable pageable) {
        return queriesService.getSessionsWithDurationHigherThanValue(durationHigherThan, pageable);
    }

    @GetMapping("/started")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<GameSession> getSessionsSortedByStartDate(Pageable pageable) {
        return queriesService.getSessionsSortedByStartDate(pageable);
    }

    @GetMapping("/earliest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public GameSession getEarliestSession() {
        return queriesService.getEarliestSession();
    }
}
