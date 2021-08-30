package com.playtika.gamesessions.controllers;

import com.playtika.gamesessions.dto.SessionDTO;
import com.playtika.gamesessions.models.GameSession;
import com.playtika.gamesessions.services.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/session")
public class GameSessionController {

    @Autowired
    GameSessionService gameSessionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<GameSession> getAll() {
        return gameSessionService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public GameSession startSession(@RequestBody SessionDTO sessionDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return gameSessionService.startGame(sessionDTO, auth.getName());
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
}
